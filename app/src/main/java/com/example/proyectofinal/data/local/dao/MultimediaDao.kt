package com.example.proyectofinal.data.local.dao

import androidx.room.*
import com.example.proyectofinal.data.local.entity.MultimediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MultimediaDao {
    @Query("SELECT * FROM multimedia WHERE reporteFolio = :folio ORDER BY creadoEn ASC")
    fun observeMultimediaForReport(folio: String): Flow<List<MultimediaEntity>>

    @Query("SELECT * FROM multimedia ORDER BY creadoEn DESC")
    fun observeAllMultimedia(): Flow<List<MultimediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultimedia(item: MultimediaEntity): Long

    @Update
    suspend fun updateMultimedia(item: MultimediaEntity)

    @Delete
    suspend fun deleteMultimedia(item: MultimediaEntity)

    @Query("DELETE FROM multimedia WHERE reporteFolio = :folio")
    suspend fun deleteByFolio(folio: String)
@Query("SELECT * FROM multimedia WHERE id = :id")
    suspend fun getMultimediaById(id: Int): MultimediaEntity?
    @Query("DELETE FROM multimedia WHERE reporteFolio = :folio AND tipo = :tipo")
    suspend fun deleteByType(folio: String, tipo: String)
}
