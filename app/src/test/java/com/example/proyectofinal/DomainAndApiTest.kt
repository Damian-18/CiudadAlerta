package com.example.proyectofinal
import com.example.proyectofinal.data.local.DemoData
import com.example.proyectofinal.data.remote.*
import com.example.proyectofinal.data.remote.dto.*
import com.example.proyectofinal.model.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class DomainAndApiTest {
    private fun api(mock: MockNetworkInterceptor = MockNetworkInterceptor()) = Retrofit.Builder().baseUrl("https://api.ciudadalerta.local/").client(OkHttpClient.Builder().addInterceptor(mock).build()).addConverterFactory(GsonConverterFactory.create()).build().create(CiudadAlertaApiService::class.java)
    @Test fun validationBoundariesAndTransitionMatrix() {
        for (n in listOf(0,19,1001)) assertNotNull(ReportValidator.validateDescription("x".repeat(n)))
        for (n in listOf(20,1000)) assertNull(ReportValidator.validateDescription("x".repeat(n)))
        for (n in listOf(0,9,201)) assertNotNull(ReportValidator.validateLocation("x".repeat(n)))
        for (n in listOf(10,200)) assertNull(ReportValidator.validateLocation("x".repeat(n)))
        for (n in listOf(0,4,501)) assertNotNull(ReportValidator.validateAdminObservation("x".repeat(n)))
        for (n in listOf(5,500)) assertNull(ReportValidator.validateAdminObservation("x".repeat(n)))
        assertNotNull(ReportValidator.validateDescription(" ".repeat(25)))
        ReportStatus.entries.forEach { from -> ReportStatus.entries.forEach { to -> assertEquals(to.ordinal == from.ordinal+1, ReportStatus.canTransition(from,to)) } }
    }
    @Test fun allHttpVerbsRoundTripAndRejectInvalidTransitions() = runTest {
        val api = api()
        val seeds = api.listarReportes().body()!!
        assertEquals(3, seeds.size)
        val new = seeds.first().copy(folio = "CA-TEST", descripcion = "Incidente de prueba con datos válidos")
        assertEquals(201, api.agregarReporte(new).code())
        assertEquals(new, api.recuperarReporte(new.folio).body())
        assertEquals(400, api.agregarReporte(new).code())
        val edited = api.editarReporte(new.folio, new.copy(ubicacion = "Avenida de prueba número 42")).body()!!
        assertEquals("Avenida de prueba número 42", edited.ubicacion)
        assertEquals(2, edited.version)
        val patch = StatusUpdateDto("RESUELTO", "Caso atendido correctamente", DemoData.RESOLVED_PHOTO, "admin@ciudadalerta.mx")
        assertEquals(400, api.actualizarEstado(new.folio,patch).code())
        assertEquals("EN_PROCESO", api.actualizarEstado(new.folio,patch.copy(estadoNuevo = "EN_PROCESO", evidenciaResueltoUri = null)).body()!!.estado)
        assertEquals(400, api.actualizarEstado(new.folio,patch.copy(evidenciaResueltoUri = null)).code())
        assertEquals(400, api.actualizarEstado(new.folio,patch.copy(responsableAdmin = "ciudadano@comunidad.mx")).code())
        val resolved = api.actualizarEstado(new.folio,patch).body()!!
        assertEquals("RESUELTO", resolved.estado)
        assertEquals(DemoData.RESOLVED_PHOTO,resolved.evidenciaResueltoUri)
        assertEquals(200, api.eliminarReporte(new.folio).code())
        assertEquals(404, api.recuperarReporte(new.folio).code())
        assertEquals(3, api.listarReportes().body()!!.size)
    }
    @Test fun authRegistrationRecoveryAndPersistence() = runTest {
        var snapshot: String? = null
        val api = api(MockNetworkInterceptor(loadAccounts = { snapshot }, saveAccounts = { snapshot = it }))
        assertEquals(UserRole.ADMIN, api.login(AuthDto("admin@ciudadalerta.mx","Demo1234")).body()!!.session.role)
        assertEquals(400, api.login(AuthDto("admin@ciudadalerta.mx","wrong")).code())
        assertEquals(400, api.register(AuthDto("invalid", "Password8", "Persona")).code())
        val dto = AuthDto("nueva@comunidad.mx", "Password8", "Nueva Vecina")
        assertEquals(UserRole.CIUDADANO, api.register(dto).body()!!.session.role)
        assertEquals(400, api.register(dto).code())
        assertEquals(200, api.recovery(RecoveryDto(dto.email,"Password9")).code())
        assertEquals(400, api.login(dto).code())
        val recreated = api(MockNetworkInterceptor(loadAccounts = { snapshot }))
        assertEquals(200, recreated.login(dto.copy(password = "Password9")).code())
    }
    @Test fun emptyHydratedStoreDoesNotReseedDeletedReports() = runTest {
        assertTrue(api(MockNetworkInterceptor(loadReports = { emptyList() })).listarReportes().body()!!.isEmpty())
    }
}
