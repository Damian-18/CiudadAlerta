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
import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
@Composable fun RecoveryScreen(vm: AuthViewModel, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Page(stringResource(R.string.recovery_title), back) {
        Text(stringResource(R.string.recovery_instructions))
        Feedback(state.busy, state.message, vm::clearMessage)
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.account_email_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.recovery(email) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.send_email_button)) }
    }
}
