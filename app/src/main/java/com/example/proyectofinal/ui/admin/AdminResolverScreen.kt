package com.example.proyectofinal.ui.admin
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
@Composable fun AdminResolverScreen(vm: AdminViewModel, folio: String, adminEmail: String, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val report = state.reports.find { it.folio == folio }
    var observation by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    Page("Gestionar $folio", back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        if (report == null) Text("Reporte no disponible") else if (report.estado == "RESUELTO") Text("Este caso ya está resuelto.") else {
            val next = if (report.estado == "PENDIENTE") ReportStatus.EN_PROCESO else ReportStatus.RESUELTO
            ReportCard(report) { Text("Siguiente estado: ${next.displayName}") }
            OutlinedTextField(observation, { observation = it }, label = { Text("Observación obligatoria") }, minLines = 3, isError = submitted && ReportValidator.validateAdminObservation(observation) != null, supportingText = { Text(if (submitted) ReportValidator.validateAdminObservation(observation) ?: "${observation.trim().length}/500" else "5 a 500 caracteres") }, modifier = Modifier.fillMaxWidth())
            if (next == ReportStatus.RESUELTO) {
                PhotoField(photo, resolved = true, onChange = { photo = it })
                if (submitted && photo == null) Text("Adjunta una fotografía para resolver el caso.", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = {
                submitted = true
                if (ReportValidator.validateAdminObservation(observation) == null && (next != ReportStatus.RESUELTO || photo != null)) vm.resolverOCambiarEstado(folio, next.code, observation, photo, adminEmail, back)
            }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Cambiar a ${next.displayName}") }
        }
    }
}
