package com.example.proyectofinal
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectofinal.data.local.*
import com.example.proyectofinal.data.local.entity.*
import com.example.proyectofinal.data.remote.*
import com.example.proyectofinal.data.remote.dto.*
import com.example.proyectofinal.data.repository.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {
    @Test fun repositoryCrudEvidenceHistoryAndDraftIsolation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()
        try {
            val api = Retrofit.Builder().baseUrl("https://api.ciudadalerta.local/").client(OkHttpClient.Builder().addInterceptor(MockNetworkInterceptor()).build()).addConverterFactory(GsonConverterFactory.create()).build().create(CiudadAlertaApiService::class.java)
            val repo = ReportRepository(db.reportDao(),db.multimediaDao(),db.notificationDao(),api,db)
            repo.syncWithRemote().getOrThrow()
            repo.syncWithRemote().getOrThrow()
            assertEquals(3,db.reportDao().countReports())
            assertEquals(4,db.multimediaDao().observeAllMultimedia().first().size)
            val new = DemoData.reports().first().copy(folio = "CA-INTEGRATION")
            repo.createReport(new).getOrThrow()
            repo.updateReport(new.copy(descripcion = "Descripción modificada en la prueba de integración")).getOrThrow()
            assertTrue(repo.getReportByFolio(new.folio)!!.descripcion.startsWith("Descripción modificada"))
            assertTrue(repo.updateReportStatusByAdmin(new.folio,"RESUELTO","Atendido correctamente",DemoData.RESOLVED_PHOTO,"admin@ciudadalerta.mx").isFailure)
            repo.updateReportStatusByAdmin(new.folio,"EN_PROCESO","Cuadrilla asignada",null,"admin@ciudadalerta.mx").getOrThrow()
            repo.updateReportStatusByAdmin(new.folio,"RESUELTO","Reparación terminada",DemoData.RESOLVED_PHOTO,"admin@ciudadalerta.mx").getOrThrow()
            assertEquals(3,repo.observeHistory(new.folio).first().size)
            assertEquals(2,db.multimediaDao().observeMultimediaForReport(new.folio).first().size)
            assertEquals("RESUELTO",repo.getReportByFolio(new.folio)!!.estado)
            val notification = db.notificationDao().observeNotificationsForUser(new.autorEmail).first().first()
            db.notificationDao().markAsRead(notification.id)
            assertTrue(db.notificationDao().getNotificationById(notification.id)!!.leida)
            val draft = DraftEntity(propietarioEmail = "a@test.mx",departamento = "BACHES",descripcion = "Borrador",ubicacion = "Ubicación",fotoLocalUri = null,actualizadoEn = 1)
            val draftId = db.draftDao().insertDraft(draft).toInt()
            db.draftDao().updateDraft(draft.copy(id = draftId,descripcion = "Editado"))
            assertEquals("Editado", db.draftDao().getDraftById(draftId)!!.descripcion)
            db.draftDao().insertDraft(draft.copy(propietarioEmail = "b@test.mx"))
            db.draftDao().clearDraftsForUser("a@test.mx")
            assertTrue(db.draftDao().observeDraftsByUser("a@test.mx").first().isEmpty())
            assertEquals(1,db.draftDao().observeDraftsByUser("b@test.mx").first().size)
            repo.deleteReport(new.folio).getOrThrow()
            assertNull(repo.getReportByFolio(new.folio))
            assertTrue(db.multimediaDao().observeMultimediaForReport(new.folio).first().isEmpty())
            assertTrue(repo.observeHistory(new.folio).first().isEmpty())
        } finally { db.close() }
    }
}
