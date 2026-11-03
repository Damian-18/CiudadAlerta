package com.example.proyectofinal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyectofinal.data.local.dao.*
import com.example.proyectofinal.data.local.entity.*

@Database(
    entities = [
        ReportEntity::class,
        DraftEntity::class,
        MultimediaEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun draftDao(): DraftDao
    abstract fun multimediaDao(): MultimediaDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ciudad_alerta_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            DemoData.reports().forEach { r ->
                                db.execSQL("INSERT INTO reportes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", arrayOf<Any?>(r.folio, r.autorEmail, r.autorNombre, r.departamento, r.descripcion, r.ubicacion, r.estado, r.fotoInicialUri, r.evidenciaResueltoUri, r.observacionAdmin, r.responsableAdmin, r.fechaCreacion, r.fechaActualizacion, r.version))
                                listOf("EVIDENCIA_INICIAL" to r.fotoInicialUri, "EVIDENCIA_RESUELTO" to r.evidenciaResueltoUri).forEach { (type, uri) ->
                                    if (uri != null) db.execSQL("INSERT INTO multimedia (reporteFolio,tipo,uriPath,mimeType,tamanoBytes,creadoEn) VALUES (?,?,?,?,?,?)", arrayOf<Any?>(r.folio,type,uri,"image/png",0,r.fechaActualizacion))
                                }
                                db.execSQL("INSERT INTO notificaciones (usuarioEmail,reporteFolio,titulo,mensaje,leida,fecha) VALUES (?,?,?,?,?,?)", arrayOf<Any?>(r.autorEmail,r.folio,"Estado: " + r.estado,r.observacionAdmin ?: "Reporte registrado",0,r.fechaActualizacion))
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
