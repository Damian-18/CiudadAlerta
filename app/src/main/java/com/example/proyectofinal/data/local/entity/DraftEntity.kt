package com.example.proyectofinal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "borradores")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propietarioEmail: String,
    val departamento: String,
    val descripcion: String,
    val ubicacion: String,
    val fotoLocalUri: String?,
    val actualizadoEn: Long
)
