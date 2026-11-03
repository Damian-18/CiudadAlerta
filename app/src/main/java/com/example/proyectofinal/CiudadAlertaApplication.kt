package com.example.proyectofinal

import android.app.Application
import com.example.proyectofinal.di.AppContainer

class CiudadAlertaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
