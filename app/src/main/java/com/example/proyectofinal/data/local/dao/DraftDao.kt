package com.example.proyectofinal.data.local.dao

import androidx.room.*
import com.example.proyectofinal.data.local.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {
    @Query("SELECT * FROM borradores WHERE propietarioEmail = :email ORDER BY actualizadoEn DESC")
    fun observeDraftsByUser(email: String): Flow<List<DraftEntity>>

    @Query("SELECT * FROM borradores WHERE id = :id LIMIT 1")
    suspend fun getDraftById(id: Int): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftEntity): Long

    @Update
    suspend fun updateDraft(draft: DraftEntity)

    @Delete
    suspend fun deleteDraft(draft: DraftEntity)

    @Query("DELETE FROM borradores WHERE id = :id")
    suspend fun deleteDraftById(id: Int)

    @Query("DELETE FROM borradores WHERE propietarioEmail = :email")
    suspend fun clearDraftsForUser(email: String)
}
