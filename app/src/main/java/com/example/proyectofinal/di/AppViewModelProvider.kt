package com.example.proyectofinal.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectofinal.CiudadAlertaApplication
import com.example.proyectofinal.ui.admin.AdminViewModel
import com.example.proyectofinal.ui.auth.AuthViewModel
import com.example.proyectofinal.ui.reportes.ReportesViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(ciudadAlertaApplication().container.draftRepository, ciudadAlertaApplication().container.authRepository)
        }
        initializer {
            val container = ciudadAlertaApplication().container
            ReportesViewModel(
                reportRepository = container.reportRepository,
                draftRepository = container.draftRepository,
                multimediaRepository = container.multimediaRepository,
                notificationRepository = container.notificationRepository
            )
        }
        initializer {
            val container = ciudadAlertaApplication().container
            AdminViewModel(
                reportRepository = container.reportRepository,
                multimediaRepository = container.multimediaRepository
            )
        }
    }
}

fun CreationExtras.ciudadAlertaApplication(): CiudadAlertaApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CiudadAlertaApplication)
