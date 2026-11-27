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
import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R
import com.example.proyectofinal.ui.*
@Composable fun AdminResolverScreen(vm: AdminViewModel, folio: String, adminEmail: String, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val report = state.reports.find { it.folio == folio }
    var observation by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    Page(stringResource(R.string.manage_case_title, folio), back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        if (report == null) Text(stringResource(R.string.report_not_available)) else if (report.estado == "RESUELTO") Text(stringResource(R.string.case_already_resolved)) else {
            val next = if (report.estado == "PENDIENTE") ReportStatus.EN_PROCESO else ReportStatus.RESUELTO
            ReportCard(report) { Text(stringResource(R.string.next_status, stringResource(next.displayNameRes))) }
            OutlinedTextField(observation, { observation = it }, label = { Text(stringResource(R.string.mandatory_observation)) }, minLines = 3, isError = submitted && ReportValidator.validateAdminObservation(observation) != null, supportingText = { Text(if (submitted) ReportValidator.validateAdminObservation(observation) ?: "${observation.trim().length}/500" else stringResource(R.string.observation_hint)) }, modifier = Modifier.fillMaxWidth())
            if (next == ReportStatus.RESUELTO) {
                PhotoField(photo, resolved = true, onChange = { photo = it })
                if (submitted && photo == null) Text(stringResource(R.string.attach_photo_to_resolve), color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = {
                submitted = true
                if (ReportValidator.validateAdminObservation(observation) == null && (next != ReportStatus.RESUELTO || photo != null)) vm.resolverOCambiarEstado(folio, next.code, observation, photo, adminEmail, back)
            }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.change_to_status, stringResource(next.displayNameRes))) }
        }
    }
}
