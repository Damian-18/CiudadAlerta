package com.example.proyectofinal.data.remote.dto

import com.example.proyectofinal.data.local.entity.ReportEntity
import com.google.gson.annotations.SerializedName

data class ReportDto(
    @SerializedName("folio") val folio: String,
    @SerializedName("autorEmail") val autorEmail: String,
    @SerializedName("autorNombre") val autorNombre: String,
    @SerializedName("departamento") val departamento: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("fotoInicialUri") val fotoInicialUri: String?,
    @SerializedName("evidenciaResueltoUri") val evidenciaResueltoUri: String?,
    @SerializedName("observacionAdmin") val observacionAdmin: String?,
    @SerializedName("responsableAdmin") val responsableAdmin: String?,
    @SerializedName("fechaCreacion") val fechaCreacion: Long,
    @SerializedName("fechaActualizacion") val fechaActualizacion: Long,
    @SerializedName("version") val version: Int = 1
)

data class StatusUpdateDto(
    @SerializedName("estadoNuevo") val estadoNuevo: String,
    @SerializedName("observacionAdmin") val observacionAdmin: String,
    @SerializedName("evidenciaResueltoUri") val evidenciaResueltoUri: String?,
    @SerializedName("responsableAdmin") val responsableAdmin: String,
    @SerializedName("fechaActualizacion") val fechaActualizacion: Long = System.currentTimeMillis()
)

fun ReportDto.toEntity(): ReportEntity = ReportEntity(
    folio = folio,
    autorEmail = autorEmail,
    autorNombre = autorNombre,
    departamento = departamento,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado,
    fotoInicialUri = fotoInicialUri,
    evidenciaResueltoUri = evidenciaResueltoUri,
    observacionAdmin = observacionAdmin,
    responsableAdmin = responsableAdmin,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    version = version
)

fun ReportEntity.toDto(): ReportDto = ReportDto(
    folio = folio,
    autorEmail = autorEmail,
    autorNombre = autorNombre,
    departamento = departamento,
    descripcion = descripcion,
    ubicacion = ubicacion,
    estado = estado,
    fotoInicialUri = fotoInicialUri,
    evidenciaResueltoUri = evidenciaResueltoUri,
    observacionAdmin = observacionAdmin,
    responsableAdmin = responsableAdmin,
    fechaCreacion = fechaCreacion,
    fechaActualizacion = fechaActualizacion,
    version = version
)
