package com.example.proyectofinal.data.remote.dto
import com.example.proyectofinal.model.UserSession
data class AuthDto(val email: String, val password: String, val nombre: String = "")
data class RecoveryDto(val email: String, val newPassword: String)
data class AuthResponse(val session: UserSession)
data class MessageDto(val message: String)
