package com.example.proyectofinal.data.remote.dto
import com.example.proyectofinal.model.UserSession

data class AuthDto(val email: String, val password: String, val data: Map<String, String>? = null)
data class RecoveryDto(val email: String)

data class SupabaseAuthResponse(
    val access_token: String?,
    val refresh_token: String?,
    val user: SupabaseUser?
)

data class SupabaseUser(
    val id: String,
    val email: String,
    val user_metadata: Map<String, String>?
)

data class PerfilDto(
    @com.google.gson.annotations.SerializedName("id") val id: String,
    @com.google.gson.annotations.SerializedName("email") val email: String?,
    @com.google.gson.annotations.SerializedName("rol") val rol: String?,
    @com.google.gson.annotations.SerializedName("nombre") val nombre: String? = null,
    @com.google.gson.annotations.SerializedName("creado_en") val creadoEn: String? = null
)
