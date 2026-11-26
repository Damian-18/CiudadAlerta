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
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
