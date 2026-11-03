package com.example.proyectofinal.ui.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectofinal.model.*
import com.example.proyectofinal.ui.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
@Composable fun RecoveryScreen(vm: AuthViewModel, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Page("Restablecer contraseña", back) {
        Text("En esta demostración local puedes asignar una contraseña nueva a una cuenta registrada. No se envían correos.")
        Feedback(state.busy, state.message, vm::clearMessage)
        OutlinedTextField(email, { email = it }, label = { Text("Correo de la cuenta") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Nueva contraseña (mínimo 8)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.recovery(email, password) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Actualizar contraseña") }
    }
}
