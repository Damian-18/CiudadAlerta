package com.example.proyectofinal.data.local.dao

import androidx.room.*
import com.example.proyectofinal.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reportes ORDER BY fechaCreacion DESC")
    fun observeAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reportes WHERE autorEmail = :email ORDER BY fechaCreacion DESC")
    fun observeReportsByUser(email: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reportes WHERE folio = :folio LIMIT 1")
    suspend fun getReportByFolio(folio: String): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("DELETE FROM reportes WHERE folio = :folio")
    suspend fun deleteByFolio(folio: String)

    @Query("SELECT COUNT(*) FROM reportes")
    suspend fun countReports(): Int
@Query("SELECT * FROM reportes")
    suspend fun getAllReports(): List<ReportEntity>
}
