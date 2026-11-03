package com.example.proyectofinal.data.repository
import com.example.proyectofinal.data.remote.CiudadAlertaApiService
import com.example.proyectofinal.data.remote.dto.*
class AuthRepository(private val api: CiudadAlertaApiService) {
    suspend fun login(email: String, password: String) = resultOf { api.login(AuthDto(email, password)).requireBody().session }
    suspend fun register(email: String, password: String, nombre: String) = resultOf { api.register(AuthDto(email, password, nombre)).requireBody().session }
    suspend fun recovery(email: String, password: String) = resultOf { api.recovery(RecoveryDto(email, password)).requireBody().message }
}
