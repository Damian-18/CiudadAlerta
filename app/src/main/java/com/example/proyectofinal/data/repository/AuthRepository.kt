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
        val token = checkNotNull(authRes.access_token) { "Error al obtener token de sesión" }
        val user = checkNotNull(authRes.user) { "Error al obtener datos del usuario" }
        authInterceptor.accessToken = token
        
        val perfilRes = api.obtenerPerfil("eq.${user.id}").requireBody()
        val perfil = perfilRes.firstOrNull() ?: throw IllegalStateException("Perfil no encontrado")
        
        val role = if (perfil.rol == "ADMIN") UserRole.ADMIN else UserRole.CIUDADANO
        val safeNombre = perfil.nombre?.takeIf { it.isNotBlank() } ?: user.email.substringBefore("@")
        
        val session = UserSession(
            uid = user.id,
            email = user.email,
            nombre = safeNombre,
            role = role
        )
        
        saveSession(token, authRes.refresh_token ?: "", session)
        session
    }

    suspend fun register(email: String, password: String, nombre: String): Result<UserSession> = resultOf {
        val authRes = api.register(AuthDto(email, password, mapOf("nombre" to nombre))).requireBody()
        
        // Supabase puede devolver un usuario sin access_token si requiere confirmación de email
        if (authRes.access_token == null) {
            throw IllegalStateException("Cuenta creada. Revisa tu correo para confirmar.")
        }
        
        val token = authRes.access_token
        val user = checkNotNull(authRes.user) { "Error al obtener datos del usuario" }
        authInterceptor.accessToken = token
        
        // Supabase creará el perfil mediante triggers en base de datos.
        // Asumimos ciudadano por defecto.
        val role = UserRole.CIUDADANO
        val session = UserSession(
            uid = user.id,
            email = user.email,
            nombre = nombre,
            role = role
        )
        saveSession(token, authRes.refresh_token ?: "", session)
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
