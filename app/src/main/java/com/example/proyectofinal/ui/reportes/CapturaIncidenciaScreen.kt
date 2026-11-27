package com.example.proyectofinal.ui.reportes
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
@Composable fun CapturaIncidenciaScreen(vm: ReportesViewModel, folio: String?, draftId: Int?, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val report = state.allReports.find { it.folio == folio }
    val draft = state.drafts.find { it.id == draftId }
    var initialized by rememberSaveable(folio, draftId) { mutableStateOf(false) }
    var department by rememberSaveable { mutableStateOf("BACHES") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(report, draft) {
        if (!initialized && (report != null || draft != null || (folio == null && draftId == null))) {
            department = report?.departamento ?: draft?.departamento ?: "BACHES"
            description = report?.descripcion ?: draft?.descripcion.orEmpty()
            location = report?.ubicacion ?: draft?.ubicacion.orEmpty()
            photo = report?.fotoInicialUri ?: draft?.fotoLocalUri
            initialized = true
        }
    }
    Page(if (folio == null) stringResource(R.string.capture_incident_title) else stringResource(R.string.edit_incident_title, folio), back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        if (!initialized) Text(stringResource(R.string.loading_report)) else {
            Choice(stringResource(R.string.department_label), Department.fromCode(department), Department.entries, { stringResource(it.displayNameRes) }, { department = it.code })
            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.description_label)) }, minLines = 3, isError = submitted && ReportValidator.validateDescription(description) != null, supportingText = { Text(if (submitted) ReportValidator.validateDescription(description) ?: "${description.trim().length}/1000" else stringResource(R.string.description_hint)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.location_label)) }, minLines = 2, isError = submitted && ReportValidator.validateLocation(location) != null, supportingText = { Text(if (submitted) ReportValidator.validateLocation(location) ?: "${location.trim().length}/200" else stringResource(R.string.location_hint)) }, modifier = Modifier.fillMaxWidth())
            PhotoField(photo, onChange = { photo = it })
            Button(onClick = {
                submitted = true
                if (ReportValidator.validateDescription(description) == null && ReportValidator.validateLocation(location) == null) vm.crearOEditarReporte(folio, draftId, department, description, location, photo, back)
            }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(if (folio == null) stringResource(R.string.submit_report_button) else stringResource(R.string.save_changes_button)) }
            if (folio == null) OutlinedButton(onClick = { vm.guardarBorrador(draftId, department, description, location, photo, back) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save_draft_button)) }
        }
    }
}
