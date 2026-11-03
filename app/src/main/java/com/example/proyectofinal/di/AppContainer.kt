package com.example.proyectofinal.di
import android.content.Context
import com.example.proyectofinal.data.local.AppDatabase
import com.example.proyectofinal.data.remote.*
import com.example.proyectofinal.data.remote.dto.toDto
import com.example.proyectofinal.data.repository.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class AppContainer(context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val preferences = context.getSharedPreferences("demo_accounts", Context.MODE_PRIVATE)
    val apiService: CiudadAlertaApiService by lazy {
        val mock = MockNetworkInterceptor(
            loadReports = { runBlocking { database.reportDao().getAllReports().map { it.toDto() } } },
            loadAccounts = { preferences.getString("accounts", null) },
            saveAccounts = { preferences.edit().putString("accounts", it).apply() }
        )
        Retrofit.Builder().baseUrl("https://api.ciudadalerta.local/")
            .client(OkHttpClient.Builder().addInterceptor(mock).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(CiudadAlertaApiService::class.java)
    }
    val reportRepository by lazy { ReportRepository(database.reportDao(), database.multimediaDao(), database.notificationDao(), apiService, database) }
    val draftRepository by lazy { DraftRepository(database.draftDao()) }
    val multimediaRepository by lazy { MultimediaRepository(database.multimediaDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val authRepository by lazy { AuthRepository(apiService) }
}
