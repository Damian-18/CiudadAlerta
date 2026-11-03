package com.example.proyectofinal.data.repository

import com.example.proyectofinal.data.local.dao.MultimediaDao
import com.example.proyectofinal.data.local.entity.MultimediaEntity
import kotlinx.coroutines.flow.Flow

class MultimediaRepository(private val multimediaDao: MultimediaDao) {
    fun observeMultimediaForReport(folio: String): Flow<List<MultimediaEntity>> =
        multimediaDao.observeMultimediaForReport(folio)

    suspend fun addMultimedia(entity: MultimediaEntity): Long =
        multimediaDao.insertMultimedia(entity)
}
