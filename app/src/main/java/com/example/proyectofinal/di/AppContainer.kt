package com.example.proyectofinal.di

import android.content.Context
import com.example.proyectofinal.BuildConfig
import com.example.proyectofinal.data.local.AppDatabase
import com.example.proyectofinal.data.remote.CiudadAlertaApiService
import com.example.proyectofinal.data.remote.SupabaseAuthInterceptor
import com.example.proyectofinal.data.repository.AuthRepository
import com.example.proyectofinal.data.repository.DraftRepository
import com.example.proyectofinal.data.repository.MultimediaRepository
import com.example.proyectofinal.data.repository.NotificationRepository
import com.example.proyectofinal.data.repository.ReportRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val database by lazy { AppDatabase.getDatabase(context) }
    private val preferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val supabaseAuthInterceptor = SupabaseAuthInterceptor()

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(supabaseAuthInterceptor)
            .addInterceptor(logging)
            .build()
    }

    val apiService: CiudadAlertaApiService by lazy {
        var baseUrl = BuildConfig.SUPABASE_URL
        if (!baseUrl.endsWith("/")) baseUrl += "/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CiudadAlertaApiService::class.java)
    }

    val reportRepository by lazy { 
        ReportRepository(
            database.reportDao(), 
            database.multimediaDao(), 
            database.notificationDao(), 
            apiService, 
            database
        ) 
    }
    
    val draftRepository by lazy { DraftRepository(database.draftDao()) }
    
    val multimediaRepository by lazy { MultimediaRepository(database.multimediaDao()) }
    
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    
    val authRepository by lazy { 
        AuthRepository(apiService, preferences, supabaseAuthInterceptor) 
    }
}
