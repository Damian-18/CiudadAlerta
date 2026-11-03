package com.example.proyectofinal.data.remote

import com.example.proyectofinal.data.remote.dto.ReportDto
import com.example.proyectofinal.data.remote.dto.StatusUpdateDto
import com.example.proyectofinal.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CiudadAlertaApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthDto): Response<AuthResponse>
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: AuthDto): Response<AuthResponse>
    @POST("api/v1/auth/recovery")
    suspend fun recovery(@Body request: RecoveryDto): Response<MessageDto>
    @GET("api/v1/reportes")
    suspend fun listarReportes(): Response<List<ReportDto>>

    @GET("api/v1/reportes/{folio}")
    suspend fun recuperarReporte(@Path("folio") folio: String): Response<ReportDto>

    @POST("api/v1/reportes")
    suspend fun agregarReporte(@Body reporte: ReportDto): Response<ReportDto>

    @PUT("api/v1/reportes/{folio}")
    suspend fun editarReporte(
        @Path("folio") folio: String,
        @Body reporte: ReportDto
    ): Response<ReportDto>

    @PATCH("api/v1/reportes/{folio}/estado")
    suspend fun actualizarEstado(
        @Path("folio") folio: String,
        @Body statusUpdate: StatusUpdateDto
    ): Response<ReportDto>

    @DELETE("api/v1/reportes/{folio}")
    suspend fun eliminarReporte(@Path("folio") folio: String): Response<Unit>
}
