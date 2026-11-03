package com.example.proyectofinal.data.repository

import com.example.proyectofinal.data.local.dao.DraftDao
import com.example.proyectofinal.data.local.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

class DraftRepository(private val draftDao: DraftDao) {
    fun observeDraftsByUser(email: String): Flow<List<DraftEntity>> =
        draftDao.observeDraftsByUser(email)

    suspend fun getDraftById(id: Int): DraftEntity? = draftDao.getDraftById(id)

    suspend fun saveDraft(draft: DraftEntity): Long = draftDao.insertDraft(draft)

    suspend fun updateDraft(draft: DraftEntity) = draftDao.updateDraft(draft)

    suspend fun deleteDraft(id: Int) = draftDao.deleteDraftById(id)

    suspend fun clearUserDrafts(email: String) = draftDao.clearDraftsForUser(email)
}
