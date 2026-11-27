package com.example.proyectofinal.data.remote.dto

import com.example.proyectofinal.data.local.entity.ReportEntity
import com.google.gson.annotations.SerializedName

data class ReportDto(
    @SerializedName("folio") val folio: String,
    @SerializedName("autor_email") val autorEmail: String,
    @SerializedName("departamento") val departamento: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("foto_inicial_url") val fotoInicialUri: String?,
    @SerializedName("evidencia_resuelto_url") val evidenciaResueltoUri: String?,
    @SerializedName("observacion_admin") val observacionAdmin: String?,
    @SerializedName("fecha_creacion") val fechaCreacion: Long,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: Long
)

data class StatusUpdateDto(
    @SerializedName("estado") val estadoNuevo: String,
    @SerializedName("observacion_admin") val observacionAdmin: String,
    @SerializedName("evidencia_resuelto_url") val evidenciaResueltoUri: String?,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: Long = System.currentTimeMillis()
)

data class HistorialReporteDto(
    @SerializedName("reporte_folio") val reporteFolio: String,
    @SerializedName("estado_anterior") val estadoAnterior: String?,
    @SerializedName("estado_nuevo") val estadoNuevo: String,
    @SerializedName("observacion") val observacion: String,
    @SerializedName("actor_email") val actorEmail: String,
    @SerializedName("fecha") val fecha: Long = System.currentTimeMillis()
)

fun ReportDto.toEntity(): ReportEntity = ReportEntity(
    folio = folio,
    autorEmail = autorEmail,
    autorNombre = autorEmail.substringBefore("@"), // Fallback if remote lacks it
    departamento = departamento,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado,
    fotoInicialUri = fotoInicialUri,
    evidenciaResueltoUri = evidenciaResueltoUri,
    observacionAdmin = observacionAdmin,
    responsableAdmin = null,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    version = 1
)

fun ReportEntity.toDto(): ReportDto = ReportDto(
    folio = folio,
    autorEmail = autorEmail,
    departamento = departamento,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado,
    fotoInicialUri = fotoInicialUri,
    evidenciaResueltoUri = evidenciaResueltoUri,
    observacionAdmin = observacionAdmin,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion
)
