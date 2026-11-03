package com.example.proyectofinal.data.local.dao

import androidx.room.*
import com.example.proyectofinal.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notificaciones WHERE usuarioEmail = :email ORDER BY fecha DESC")
    fun observeNotificationsForUser(email: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notificaciones WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)
@Query("SELECT * FROM notificaciones WHERE id = :id")
    suspend fun getNotificationById(id: Int): NotificationEntity?
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    @Query("SELECT * FROM notificaciones WHERE reporteFolio = :folio ORDER BY fecha ASC")
    fun observeHistory(folio: String): Flow<List<NotificationEntity>>
    @Query("DELETE FROM notificaciones WHERE reporteFolio = :folio")
    suspend fun deleteByFolio(folio: String)
}
