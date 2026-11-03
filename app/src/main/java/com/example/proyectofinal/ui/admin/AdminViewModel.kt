package com.example.proyectofinal.ui.admin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.local.entity.ReportEntity
import com.example.proyectofinal.data.repository.*
import com.example.proyectofinal.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
data class AdminUiState(val reports: List<ReportEntity> = emptyList(), val busy: Boolean = false, val message: String? = null) {
    val total get() = reports.size
    val pendientes get() = reports.count { it.estado == "PENDIENTE" }
    val enProceso get() = reports.count { it.estado == "EN_PROCESO" }
    val resueltos get() = reports.count { it.estado == "RESUELTO" }
    val porcentajeAtencion get() = if (total == 0) 0f else 100f * resueltos / total
    val departments get() = Department.entries.map { d ->
        val list = reports.filter { it.departamento == d.code }
        DepartmentKpi(d, list.size, list.count { it.estado == "PENDIENTE" }, list.count { it.estado == "EN_PROCESO" }, list.count { it.estado == "RESUELTO" })
    }
}
class AdminViewModel(private val reportRepository: ReportRepository, private val multimediaRepository: MultimediaRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()
    private var session: UserSession? = null
    init { viewModelScope.launch { reportRepository.observeAllReports().catch { e -> _uiState.update { it.copy(message = e.message) } }.collect { reports -> _uiState.update { it.copy(reports = reports) } } } }
    fun bind(value: UserSession?) { session = value; _uiState.update { it.copy(message = null) } }
    fun clearMessage() { _uiState.update { it.copy(message = null) } }
    fun resolverOCambiarEstado(folio: String, nuevoEstado: String, observacion: String, evidenciaResueltoUri: String?, adminEmail: String, onSuccess: () -> Unit) {
        if (_uiState.value.busy) return
        _uiState.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val result = resultOf {
                require(session?.role == UserRole.ADMIN && session?.email == adminEmail) { "Acceso reservado a administradores" }
                reportRepository.updateReportStatusByAdmin(folio, nuevoEstado, observacion, evidenciaResueltoUri, adminEmail).getOrThrow()
            }
            _uiState.update { it.copy(busy = false, message = result.exceptionOrNull()?.message ?: "Estado actualizado") }
            if (result.isSuccess) onSuccess()
        }
    }
}
