package com.example.proyectofinal

import com.example.proyectofinal.data.remote.CiudadAlertaApiService
import com.example.proyectofinal.data.remote.MockNetworkInterceptor
import com.example.proyectofinal.data.remote.dto.AuthDto
import com.example.proyectofinal.data.remote.dto.RecoveryDto
import com.example.proyectofinal.model.UserRole
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DemoAccessTest {
    private fun api(load: () -> String? = { null }, save: (String) -> Unit = {}) =
        Retrofit.Builder()
            .baseUrl("https://api.ciudadalerta.local/")
            .client(OkHttpClient.Builder().addInterceptor(MockNetworkInterceptor(
                loadAccounts = load, saveAccounts = save
            )).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CiudadAlertaApiService::class.java)

    private val demos = mapOf(
        "ciudadano@comunidad.mx" to UserRole.CIUDADANO,
        "admin@ciudadalerta.mx" to UserRole.ADMIN
    )

    @Test fun demoRecoveryIsRejectedWithoutChangingQuickAccess() = runTest {
        var snapshot: String? = null
        val service = api({ snapshot }, { snapshot = it })
        demos.forEach { (email, role) ->
            val response = service.recovery(RecoveryDto("  ${email.uppercase()}  ", "Nueva1234"))
            assertEquals(400, response.code())
            assertTrue(response.errorBody()!!.string().contains("Demo1234"))
            assertEquals(role, service.login(AuthDto(email, "Demo1234")).body()!!.session.role)
            assertEquals(400, service.login(AuthDto(email, "Nueva1234")).code())
        }
        assertNull("A rejected recovery must not persist account changes", snapshot)
        val restarted = api({ snapshot })
        demos.forEach { (email, role) ->
            assertEquals(role, restarted.login(AuthDto(email, "Demo1234")).body()!!.session.role)
        }
    }

    @Test fun legacyDemoPasswordsAreRepairedWithoutLosingRegisteredAccounts() = runTest {
        var snapshot: String? = """[
            {"email":"ciudadano@comunidad.mx","password":"Anterior123","nombre":"Juan Ciudadano","role":"CIUDADANO"},
            {"email":"admin@ciudadalerta.mx","password":"Anterior123","nombre":"Administración","role":"ADMIN"},
            {"email":"vecina@example.com","password":"Personal123","nombre":"Vecina","role":"CIUDADANO"}
        ]"""
        val service = api({ snapshot }, { snapshot = it })
        demos.forEach { (email, role) ->
            assertEquals(role, service.login(AuthDto(email, "Demo1234")).body()!!.session.role)
            assertEquals(400, service.login(AuthDto(email, "Anterior123")).code())
        }
        assertEquals(200, service.login(AuthDto("vecina@example.com", "Personal123")).code())
        assertEquals(200, service.recovery(RecoveryDto("vecina@example.com", "Personal456")).code())
        val restarted = api({ snapshot })
        assertEquals(400, restarted.login(AuthDto("vecina@example.com", "Personal123")).code())
        assertEquals(200, restarted.login(AuthDto("vecina@example.com", "Personal456")).code())
        demos.forEach { (email, role) ->
            assertEquals(role, restarted.login(AuthDto(email, "Demo1234")).body()!!.session.role)
        }
    }
}
