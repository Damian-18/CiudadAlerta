package com.example.proyectofinal.data.remote

import com.example.proyectofinal.data.remote.dto.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CiudadAlertaApiService {
    @POST("auth/v1/token?grant_type=password")
    suspend fun login(@Body request: AuthDto): Response<SupabaseAuthResponse>
    
    @POST("auth/v1/signup")
    suspend fun register(@Body request: AuthDto): Response<SupabaseAuthResponse>
    
    @POST("auth/v1/recover")
    suspend fun recovery(@Body request: RecoveryDto): Response<Unit>
    
    @GET("rest/v1/perfiles")
    suspend fun obtenerPerfil(
        @Query("id") eqId: String,
        @Query("select") select: String = "*"
    ): Response<List<PerfilDto>>

    @GET("rest/v1/reportes")
    suspend fun listarReportes(
        @Query("select") select: String = "*",
        @Query("order") order: String = "fechaCreacion.desc"
    ): Response<List<ReportDto>>

    @GET("rest/v1/reportes")
    suspend fun recuperarReporte(
        @Query("folio") eqFolio: String,
        @Query("select") select: String = "*"
    ): Response<List<ReportDto>>

    @POST("rest/v1/reportes")
    suspend fun agregarReporte(@Body reporte: ReportDto): Response<List<ReportDto>>

    @PATCH("rest/v1/reportes")
    suspend fun editarReporte(
        @Query("folio") eqFolio: String,
        @Body reporte: ReportDto
    ): Response<List<ReportDto>>

    // Actualizar Estado se puede hacer con un PATCH a la misma tabla 'reportes'
    @PATCH("rest/v1/reportes")
    suspend fun actualizarEstado(
        @Query("folio") eqFolio: String,
        @Body statusUpdate: StatusUpdateDto
    ): Response<List<ReportDto>>

    @DELETE("rest/v1/reportes")
    suspend fun eliminarReporte(@Query("folio") eqFolio: String): Response<Unit>

    // Storage Supabase
    @POST("storage/v1/object/evidencias/{rutaArchivo}")
    suspend fun subirEvidencia(
        @Path("rutaArchivo") rutaArchivo: String,
        @Body file: RequestBody,
        @Header("Content-Type") contentType: String = "image/jpeg"
    ): Response<ResponseBody>
}
