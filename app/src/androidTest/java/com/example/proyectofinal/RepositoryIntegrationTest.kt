package com.example.proyectofinal

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectofinal.data.local.AppDatabase
import com.example.proyectofinal.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var draftRepo: DraftRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        draftRepo = DraftRepository(db.draftDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDraftsIsolated() = runBlocking {
        draftRepo.saveDraft(
            com.example.proyectofinal.data.local.entity.DraftEntity(
                propietarioEmail = "user1@demo.com",
                departamento = "BACHES",
                descripcion = "Desc 1",
                ubicacion = "Loc 1",
                fotoLocalUri = null,
                actualizadoEn = System.currentTimeMillis()
            )
        )
        
        draftRepo.saveDraft(
            com.example.proyectofinal.data.local.entity.DraftEntity(
                propietarioEmail = "user2@demo.com",
                departamento = "ALUMBRADO",
                descripcion = "Desc 2",
                ubicacion = "Loc 2",
                fotoLocalUri = null,
                actualizadoEn = System.currentTimeMillis()
            )
        )
        
        draftRepo.clearUserDrafts("user1@demo.com")
        
        val u1 = draftRepo.observeDraftsByUser("user1@demo.com").first()
        val u2 = draftRepo.observeDraftsByUser("user2@demo.com").first()
        
        assert(u1.isEmpty())
        assert(u2.size == 1)
    }
}
