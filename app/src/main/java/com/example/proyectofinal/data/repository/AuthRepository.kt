package com.example.proyectofinal.data.repository

import android.content.SharedPreferences
import com.example.proyectofinal.data.remote.CiudadAlertaApiService
import com.example.proyectofinal.data.remote.SupabaseAuthInterceptor
import com.example.proyectofinal.data.remote.dto.*
import com.example.proyectofinal.model.UserRole
import com.example.proyectofinal.model.UserSession

class AuthRepository(
    private val api: CiudadAlertaApiService,
    private val prefs: SharedPreferences,
    private val authInterceptor: SupabaseAuthInterceptor
) {
    init {
        // Restaurar sesión al iniciar
        val token = prefs.getString("access_token", null)
        if (token != null) {
            authInterceptor.accessToken = token
        }
    }

    suspend fun login(email: String, password: String): Result<UserSession> = resultOf {
        val authRes = api.login(AuthDto(email, password)).requireBody()
        authInterceptor.accessToken = authRes.access_token
        
        val perfilRes = api.obtenerPerfil("eq.${authRes.user.id}").requireBody()
        val perfil = perfilRes.firstOrNull() ?: throw IllegalStateException("Perfil no encontrado")
        
        val role = if (perfil.rol == "ADMIN") UserRole.ADMIN else UserRole.CIUDADANO
        val session = UserSession(
            uid = authRes.user.id,
            email = authRes.user.email,
            nombre = perfil.nombre,
            role = role
        )
        
        saveSession(authRes.access_token, authRes.refresh_token, session)
        session
    }

    suspend fun register(email: String, password: String, nombre: String): Result<UserSession> = resultOf {
        val authRes = api.register(AuthDto(email, password, mapOf("nombre" to nombre))).requireBody()
        authInterceptor.accessToken = authRes.access_token
        
        // Supabase creará el perfil mediante triggers en base de datos.
        // Hacemos polling o asumimos ciudadano por defecto.
        val role = UserRole.CIUDADANO
        val session = UserSession(
            uid = authRes.user.id,
            email = authRes.user.email,
            nombre = nombre,
            role = role
        )
        saveSession(authRes.access_token, authRes.refresh_token, session)
        session
    }

    suspend fun recovery(email: String): Result<String> = resultOf {
        api.recovery(RecoveryDto(email)).requireSuccessful()
        "Correo de recuperación enviado"
    }

    fun logout() {
        authInterceptor.accessToken = null
        prefs.edit().clear().apply()
    }

    private fun saveSession(accessToken: String, refreshToken: String, session: UserSession) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putString("uid", session.uid)
            .putString("email", session.email)
            .putString("nombre", session.nombre)
            .putString("role", session.role.name)
            .apply()
    }

    fun getCurrentSession(): UserSession? {
        val token = prefs.getString("access_token", null) ?: return null
        val uid = prefs.getString("uid", null) ?: return null
        val email = prefs.getString("email", null) ?: return null
        val nombre = prefs.getString("nombre", null) ?: return null
        val roleName = prefs.getString("role", null) ?: return null
        return UserSession(uid, email, nombre, UserRole.valueOf(roleName))
    }
}
