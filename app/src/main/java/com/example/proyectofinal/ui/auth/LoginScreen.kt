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
@Composable fun LoginScreen(vm: AuthViewModel, register: () -> Unit, recovery: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Page("Ciudad Alerta") {
        Text("Tu comunidad, mejor atendida", style = MaterialTheme.typography.headlineMedium)
        Text("Reporta baches, fallas de alumbrado y acumulación de basura. Consulta el seguimiento de cada caso.")
        Feedback(state.busy, state.message, vm::clearMessage)
        OutlinedTextField(email, { email = it }, label = { Text("Correo electrónico") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.login(email, password) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
        TextButton(onClick = register, enabled = !state.busy) { Text("Crear cuenta ciudadana") }
        TextButton(onClick = recovery, enabled = !state.busy) { Text("Restablecer contraseña") }
        HorizontalDivider()
        Text("Explorar la demostración", style = MaterialTheme.typography.titleMedium)
        Text("Las cuentas demo usan la contraseña fija Demo1234, incluso si la cambiaste en una versión anterior. Para usar tu propia contraseña, crea una cuenta ciudadana.", style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = vm::loginDemoCiudadano, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Demo Ciudadano") }
        OutlinedButton(onClick = vm::loginDemoAdmin, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Demo Admin") }
        Text("Para incidencias comunitarias no urgentes.", style = MaterialTheme.typography.bodySmall)
    }
}
