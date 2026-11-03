package com.example.proyectofinal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reportes")
data class ReportEntity(
    @PrimaryKey val folio: String,
    val autorEmail: String,
    val autorNombre: String,
    val departamento: String,          // BACHES | ALUMBRADO | BASURA
    val descripcion: String,           // 20..1000 chars
    val ubicacion: String,             // 10..200 chars
    val estado: String,                // PENDIENTE | EN_PROCESO | RESUELTO
    val fotoInicialUri: String?,       // Evidencia ciudadana
    val evidenciaResueltoUri: String?, // Evidencia cuando el caso es resuelto por Admin
    val observacionAdmin: String?,     // Observación del cambio de estado
    val responsableAdmin: String?,     // Correo del administrador que atendió
    val fechaCreacion: Long,
    val fechaActualizacion: Long,
    val version: Int = 1
)
