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
import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R

@Composable fun LoginScreen(vm: AuthViewModel, register: () -> Unit, recovery: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Page("Ciudad Alerta") {
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.login_subtitle))
        Feedback(state.busy, state.message, vm::clearMessage)
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password_label)) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.login(email, password) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.login_button)) }
        TextButton(onClick = register, enabled = !state.busy) { Text(stringResource(R.string.create_account_button)) }
        TextButton(onClick = recovery, enabled = !state.busy) { Text(stringResource(R.string.recovery_button)) }
        Text("Para incidencias comunitarias no urgentes.", style = MaterialTheme.typography.bodySmall)
    }
}
