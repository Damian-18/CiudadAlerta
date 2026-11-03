package com.example.proyectofinal.data.repository

import com.example.proyectofinal.data.local.dao.NotificationDao
import com.example.proyectofinal.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {
    fun observeNotifications(email: String): Flow<List<NotificationEntity>> =
        notificationDao.observeNotificationsForUser(email)

    suspend fun markAsRead(id: Int) = notificationDao.markAsRead(id)

    suspend fun deleteNotification(id: Int) = notificationDao.deleteNotificationById(id)
}
