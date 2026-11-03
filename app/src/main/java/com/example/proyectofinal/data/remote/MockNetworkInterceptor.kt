package com.example.proyectofinal.data.remote
import com.example.proyectofinal.data.local.DemoData
import com.example.proyectofinal.data.remote.dto.*
import com.example.proyectofinal.model.*
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/** Autonomous JSON backend. All requests terminate here; no DNS or external server. */
class MockNetworkInterceptor(
    private val loadReports: () -> List<ReportDto> = { DemoData.reports().map { it.toDto() } },
    private val loadAccounts: () -> String? = { null },
    private val saveAccounts: (String) -> Unit = { _ -> Unit }
) : Interceptor {
    private val gson = Gson()
    private val lock = Any()
    private val reports by lazy { loadReports().associateBy { it.folio }.toMutableMap() }
    private data class Account(val email: String, val password: String, val nombre: String, val role: UserRole)
    private val demoAccounts = listOf(
        Account("ciudadano@comunidad.mx", "Demo1234", "Juan Ciudadano", UserRole.CIUDADANO),
        Account("admin@ciudadalerta.mx", "Demo1234", "Administración", UserRole.ADMIN)
    )
    private val accounts by lazy {
        val saved = loadAccounts()?.let { gson.fromJson(it, Array<Account>::class.java).toList() }.orEmpty()
        // Reserved demo identities take precedence over passwords saved by older versions.
        // Registered accounts keep their persisted credentials and recovery support.
        (saved + demoAccounts).associateBy { it.email }.toMutableMap()
    }
    override fun intercept(chain: Interceptor.Chain): Response = synchronized(lock) {
        val request = chain.request()
        val segments = request.url.pathSegments
        var code = 200
        var payload: Any = mapOf("error" to "Ruta no encontrada")
        try {
            require(request.url.host == "api.ciudadalerta.local" && segments.take(2) == listOf("api", "v1")) { "Ruta no permitida" }
            if (segments.getOrNull(2) == "auth" && request.method == "POST") {
                val action = segments.last()
                if (action == "recovery") {
                    val dto = gson.fromJson(request.bodyText(), RecoveryDto::class.java)
                    val email = dto.email.trim().lowercase()
                    val account = accounts[email] ?: error("No existe una cuenta con ese correo")
                    require(demoAccounts.none { it.email == email }) {
                        "Las cuentas demo usan la contraseña fija Demo1234. Crea una cuenta ciudadana para probar la recuperación."
                    }
                    require(dto.newPassword.length >= 8) { "La contraseña requiere al menos 8 caracteres" }
                    accounts[email] = account.copy(password = dto.newPassword)
                    saveAccounts(gson.toJson(accounts.values))
                    payload = MessageDto("Contraseña actualizada en la demostración local.")
                } else {
                    val dto = gson.fromJson(request.bodyText(), AuthDto::class.java)
                    val email = dto.email.trim().lowercase()
                    require(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email)) { "Correo no válido" }
                    val account = when (action) {
                        "register" -> {
                            require(email !in accounts) { "Este correo ya está registrado" }
                            require(dto.nombre.trim().length >= 2 && dto.password.length >= 8) { "Nombre y contraseña de al menos 8 caracteres requeridos" }
                            Account(email, dto.password, dto.nombre.trim(), UserRole.CIUDADANO).also { accounts[email] = it; saveAccounts(gson.toJson(accounts.values)); code = 201 }
                        }
                        "login" -> accounts[email]?.takeIf { it.password == dto.password } ?: error("Correo o contraseña incorrectos")
                        else -> error("Operación de autenticación no encontrada")
                    }
                    payload = AuthResponse(UserSession(account.email, account.email, account.nombre, account.role))
                }
            } else if (segments.getOrNull(2) == "reportes") {
                val folio = segments.getOrNull(3)
                when (request.method) {
                    "GET" -> if (folio == null) payload = reports.values.toList() else {
                        payload = reports[folio] ?: mapOf("error" to "Reporte no encontrado").also { code = 404 }
                    }
                    "POST" -> {
                        require(folio == null)
                        val incoming = gson.fromJson(request.bodyText(), ReportDto::class.java)
                        validate(incoming)
                        require(incoming.folio !in reports) { "Folio duplicado" }
                        require(incoming.estado == "PENDIENTE")
                        reports[incoming.folio] = incoming; payload = incoming; code = 201
                    }
                    "PUT" -> {
                        val current = reports[folio] ?: error("Reporte no encontrado")
                        val incoming = gson.fromJson(request.bodyText(), ReportDto::class.java)
                        validate(incoming)
                        require(incoming.folio == folio && incoming.autorEmail == current.autorEmail && incoming.estado == current.estado) { "No se puede cambiar autor o estado mediante edición" }
                        val updated = current.copy(departamento = incoming.departamento, descripcion = incoming.descripcion, ubicacion = incoming.ubicacion, fotoInicialUri = incoming.fotoInicialUri, fechaActualizacion = System.currentTimeMillis(), version = current.version + 1)
                        reports[current.folio] = updated; payload = updated
                    }
                    "PATCH" -> {
                        require(segments.last() == "estado")
                        val current = reports[folio] ?: error("Reporte no encontrado")
                        val patch = gson.fromJson(request.bodyText(), StatusUpdateDto::class.java)
                        val next = ReportStatus.valueOf(patch.estadoNuevo)
                        require(ReportStatus.canTransition(ReportStatus.valueOf(current.estado), next)) { "Transición no permitida" }
                        require(ReportValidator.validateAdminObservation(patch.observacionAdmin) == null) { "Observación no válida" }
                        require(accounts[patch.responsableAdmin]?.role == UserRole.ADMIN) { "Administrador requerido" }
                        require(next != ReportStatus.RESUELTO || !patch.evidenciaResueltoUri.isNullOrBlank()) { "Adjunta evidencia de resolución" }
                        val updated = current.copy(estado = next.code, observacionAdmin = patch.observacionAdmin.trim(), evidenciaResueltoUri = patch.evidenciaResueltoUri ?: current.evidenciaResueltoUri, responsableAdmin = patch.responsableAdmin, fechaActualizacion = patch.fechaActualizacion, version = current.version + 1)
                        reports[current.folio] = updated; payload = updated
                    }
                    "DELETE" -> { reports.remove(folio); payload = mapOf("deleted" to folio) }
                    else -> code = 405
                }
            } else code = 404
        } catch (e: IllegalArgumentException) { code = 400; payload = mapOf("error" to (e.message ?: "Datos no válidos"))
        } catch (e: IllegalStateException) { code = 400; payload = mapOf("error" to (e.message ?: "Operación no válida")) }
        Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(code).message(if (code < 300) "OK" else "Invalid request")
            .body(gson.toJson(payload).toResponseBody("application/json; charset=utf-8".toMediaType())).build()
    }
    private fun validate(report: ReportDto) {
        require(ReportValidator.validateDescription(report.descripcion) == null) { "Descripción no válida" }
        require(ReportValidator.validateLocation(report.ubicacion) == null) { "Ubicación no válida" }
        Department.valueOf(report.departamento)
    }
    private fun Request.bodyText(): String = Buffer().also { body?.writeTo(it) }.readUtf8()
}
