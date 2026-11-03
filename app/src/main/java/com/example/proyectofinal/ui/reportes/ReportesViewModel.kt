package com.example.proyectofinal.ui.reportes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.local.entity.*
import com.example.proyectofinal.data.repository.*
import com.example.proyectofinal.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
data class ReportesUiState(
    val allReports: List<ReportEntity> = emptyList(), val showOnlyMyReports: Boolean = false,
    val selectedDepartment: Department? = null, val selectedStatus: ReportStatus? = null,
    val drafts: List<DraftEntity> = emptyList(), val notifications: List<NotificationEntity> = emptyList(),
    val session: UserSession? = null, val busy: Boolean = false, val message: String? = null
) {
    val filteredReports get() = allReports.filter { (!showOnlyMyReports || it.autorEmail == session?.email) && (selectedDepartment == null || it.departamento == selectedDepartment.code) && (selectedStatus == null || it.estado == selectedStatus.code) }
}
class ReportesViewModel(
    private val reportRepository: ReportRepository, private val draftRepository: DraftRepository,
    private val multimediaRepository: MultimediaRepository, private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState = _uiState.asStateFlow()
    private var subscription: Job? = null
    fun bind(session: UserSession?) {
        if (_uiState.value.session == session) return
        subscription?.cancel()
        _uiState.value = ReportesUiState(session = session)
        if (session == null) return
        subscription = viewModelScope.launch {
            combine(reportRepository.observeAllReports(), draftRepository.observeDraftsByUser(session.email), notificationRepository.observeNotifications(session.email)) { reports, drafts, notifications -> Triple(reports, drafts, notifications) }
                .catch { e -> _uiState.update { it.copy(message = e.message) } }
                .collect { (reports, drafts, notifications) -> _uiState.update { it.copy(allReports = reports, drafts = drafts, notifications = notifications) } }
        }
        syncRemoteReports()
    }
    fun onlyMine(value: Boolean) { _uiState.update { it.copy(showOnlyMyReports = value) } }
    fun department(value: Department?) { _uiState.update { it.copy(selectedDepartment = value) } }
    fun status(value: ReportStatus?) { _uiState.update { it.copy(selectedStatus = value) } }
    fun clearMessage() { _uiState.update { it.copy(message = null) } }
    private fun action(success: String, onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = resultOf(block)
            _uiState.update { it.copy(busy = false, message = result.exceptionOrNull()?.message ?: success) }
            if (result.isSuccess) onSuccess()
        }
    }
    fun syncRemoteReports() = action("Reportes actualizados") { reportRepository.syncWithRemote().getOrThrow() }
    fun crearOEditarReporte(folio: String?, draftId: Int?, departamento: String, descripcion: String, ubicacion: String, foto: String?, onSuccess: () -> Unit) = action("Reporte guardado", onSuccess) {
        val session = checkNotNull(_uiState.value.session) { "Inicia sesión" }
        require(session.role == UserRole.CIUDADANO)
        val existing = if (folio != null) checkNotNull(reportRepository.getReportByFolio(folio)) { "Reporte no encontrado" } else null
        require(existing == null || existing.autorEmail == session.email) { "Solo puedes editar tus propios reportes" }
        if (draftId != null) require(draftRepository.getDraftById(draftId)?.propietarioEmail == session.email) { "Borrador no disponible" }
        val now = System.currentTimeMillis()
        val report = existing?.copy(departamento = departamento, descripcion = descripcion.trim(), ubicacion = ubicacion.trim(), fotoInicialUri = foto)
            ?: ReportEntity("CA-" + UUID.randomUUID().toString().take(12).uppercase(), session.email, session.nombre, departamento, descripcion.trim(), ubicacion.trim(), "PENDIENTE", foto, null, null, null, now, now)
        if (existing == null) reportRepository.createReport(report).getOrThrow() else reportRepository.updateReport(report).getOrThrow()
        if (draftId != null) draftRepository.deleteDraft(draftId)
    }
    fun eliminarReporte(folio: String) = action("Reporte eliminado") {
        val report = checkNotNull(reportRepository.getReportByFolio(folio))
        require(report.autorEmail == _uiState.value.session?.email && _uiState.value.session?.role == UserRole.CIUDADANO) { "Solo puedes eliminar tus reportes" }
        reportRepository.deleteReport(folio).getOrThrow()
    }
    fun guardarBorrador(id: Int?, departamento: String, descripcion: String, ubicacion: String, foto: String?, onSuccess: () -> Unit) = action("Borrador guardado", onSuccess) {
        val session = checkNotNull(_uiState.value.session)
        require(session.role == UserRole.CIUDADANO)
        if (id != null) require(draftRepository.getDraftById(id)?.propietarioEmail == session.email)
        draftRepository.saveDraft(DraftEntity(id ?: 0, session.email, departamento, descripcion, ubicacion, foto, System.currentTimeMillis()))
    }
    fun eliminarBorrador(id: Int) = action("Borrador eliminado") {
        require(draftRepository.getDraftById(id)?.propietarioEmail == _uiState.value.session?.email)
        draftRepository.deleteDraft(id)
    }
    fun marcarNotificacionLeida(id: Int) = action("Notificación leída") { notificationRepository.markAsRead(id) }
    fun eliminarNotificacion(id: Int) = action("Notificación eliminada") { notificationRepository.deleteNotification(id) }
    fun history(folio: String) = reportRepository.observeHistory(folio)
    fun multimedia(folio: String) = multimediaRepository.observeMultimediaForReport(folio)
}
