package com.example.proyectofinal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "multimedia")
data class MultimediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reporteFolio: String,
    val tipo: String, // EVIDENCIA_INICIAL | EVIDENCIA_RESUELTO
    val uriPath: String,
    val mimeType: String = "image/jpeg",
    val tamanoBytes: Long = 102400L,
    val creadoEn: Long = System.currentTimeMillis()
)
