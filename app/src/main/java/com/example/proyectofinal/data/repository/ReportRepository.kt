package com.example.proyectofinal.data.repository
import androidx.room.withTransaction
import com.example.proyectofinal.data.local.AppDatabase
import com.example.proyectofinal.data.local.dao.*
import com.example.proyectofinal.data.local.entity.*
import com.example.proyectofinal.data.remote.CiudadAlertaApiService
import com.example.proyectofinal.data.remote.dto.*
import com.example.proyectofinal.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReportRepository(
    private val reportDao: ReportDao,
    private val multimediaDao: MultimediaDao,
    private val notificationDao: NotificationDao,
    private val apiService: CiudadAlertaApiService,
    private val database: AppDatabase
) {
    private val mutex = Mutex()
    fun observeAllReports() = reportDao.observeAllReports()
    fun observeReportsByUser(email: String) = reportDao.observeReportsByUser(email)
    fun observeHistory(folio: String) = notificationDao.observeHistory(folio)
    suspend fun getReportByFolio(folio: String): ReportEntity? = reportDao.getReportByFolio(folio) ?: apiService.recuperarReporte(folio).let { response ->
        if (response.code() == 404) null else response.requireBody().toEntity().also { reportDao.insertReport(it) }
    }
    private suspend fun persist(report: ReportEntity) {
        reportDao.insertReport(report)
        for ((type, uri) in listOf(MultimediaType.EVIDENCIA_INICIAL to report.fotoInicialUri, MultimediaType.EVIDENCIA_RESUELTO to report.evidenciaResueltoUri)) {
            multimediaDao.deleteByType(report.folio, type.code)
            if (!uri.isNullOrBlank()) multimediaDao.insertMultimedia(MultimediaEntity(reporteFolio = report.folio, tipo = type.code, uriPath = uri))
        }
    }
    suspend fun syncWithRemote(): Result<Unit> = resultOf { mutex.withLock {
        val reports = apiService.listarReportes().requireBody()
        database.withTransaction { reports.forEach { persist(it.toEntity()) } }
    } }
    suspend fun createReport(report: ReportEntity): Result<ReportEntity> = resultOf { mutex.withLock {
        validate(report)
        val confirmed = apiService.agregarReporte(report.toDto()).requireBody().toEntity()
        database.withTransaction {
            persist(confirmed)
            notificationDao.insertNotification(NotificationEntity(usuarioEmail = confirmed.autorEmail, reporteFolio = confirmed.folio, titulo = "Reporte registrado: ${confirmed.folio}", mensaje = "Reporte recibido en ${confirmed.departamento}: PENDIENTE."))
        }
        confirmed
    } }
    suspend fun updateReport(report: ReportEntity): Result<ReportEntity> = resultOf { mutex.withLock {
        validate(report)
        val confirmed = apiService.editarReporte(report.folio, report.toDto()).requireBody().toEntity()
        database.withTransaction { persist(confirmed) }
        confirmed
    } }
    suspend fun updateReportStatusByAdmin(folio: String, nuevoEstado: String, observacion: String, evidenciaResueltoUri: String?, adminEmail: String): Result<ReportEntity> = resultOf { mutex.withLock {
        val current = checkNotNull(reportDao.getReportByFolio(folio)) { "Reporte no encontrado" }
        require(ReportStatus.canTransition(ReportStatus.valueOf(current.estado), ReportStatus.valueOf(nuevoEstado))) { "Transición no permitida" }
        require(ReportValidator.validateAdminObservation(observacion) == null) { "La observación debe tener de 5 a 500 caracteres" }
        require(nuevoEstado != "RESUELTO" || !evidenciaResueltoUri.isNullOrBlank()) { "La evidencia fotográfica es obligatoria" }
        val updated = apiService.actualizarEstado(folio, StatusUpdateDto(nuevoEstado, observacion.trim(), evidenciaResueltoUri, adminEmail)).requireBody().toEntity()
        database.withTransaction {
            persist(updated)
            notificationDao.insertNotification(NotificationEntity(usuarioEmail = updated.autorEmail, reporteFolio = folio, titulo = "Estado: $nuevoEstado", mensaje = "$adminEmail: ${observacion.trim()}"))
        }
        updated
    } }
    suspend fun deleteReport(folio: String): Result<Unit> = resultOf { mutex.withLock {
        val response = apiService.eliminarReporte(folio)
        check(response.isSuccessful) { "No fue posible eliminar el reporte" }
        database.withTransaction { multimediaDao.deleteByFolio(folio); notificationDao.deleteByFolio(folio); reportDao.deleteByFolio(folio) }
    } }
    private fun validate(report: ReportEntity) {
        require(ReportValidator.validateDescription(report.descripcion) == null) { "Descripción: 20 a 1000 caracteres" }
        require(ReportValidator.validateLocation(report.ubicacion) == null) { "Ubicación: 10 a 200 caracteres" }
    }
}
