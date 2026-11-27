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
@Composable fun RegisterScreen(vm: AuthViewModel, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Page(stringResource(R.string.register_title), back) {
        Text(stringResource(R.string.register_subtitle), style = MaterialTheme.typography.headlineSmall)
        Feedback(state.busy, state.message, vm::clearMessage)
        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name_label)) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.email_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password_min_8_label)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button(onClick = { vm.register(email, password, name) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.create_account_button)) }
        Text(stringResource(R.string.demo_account_saved))
    }
}
