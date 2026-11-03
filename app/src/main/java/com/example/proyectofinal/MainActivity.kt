package com.example.proyectofinal
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectofinal.di.AppViewModelProvider
import com.example.proyectofinal.ui.AppNavigation
import com.example.proyectofinal.ui.theme.CiudadAlertaTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { CiudadAlertaTheme { AppNavigation(AppViewModelProvider.Factory) } }
    }
}
