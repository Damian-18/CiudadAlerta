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
@Composable fun ListadoReportesScreen(vm: ReportesViewModel, newReport: () -> Unit, detail: (String) -> Unit, edit: (String) -> Unit, logout: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var deleteFolio by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    Page(stringResource(R.string.community_incidents_title)) {
        Text(stringResource(R.string.hello_user, state.session?.nombre.orEmpty()), style = MaterialTheme.typography.titleLarge)
        Feedback(state.busy, state.message, vm::clearMessage)
        Button(onClick = newReport, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.report_incident_button)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (state.showOnlyMyReports) stringResource(R.string.my_reports) else stringResource(R.string.all_incidents))
            Switch(checked = state.showOnlyMyReports, onCheckedChange = vm::onlyMine)
        }
        Filters(state.selectedDepartment, state.selectedStatus, vm::department, vm::status)
        OutlinedButton(onClick = vm::syncRemoteReports, enabled = !state.busy) { Text(stringResource(R.string.update_reports_button)) }
        Text(stringResource(R.string.incidents_count, state.filteredReports.size), style = MaterialTheme.typography.titleMedium)
        if (state.filteredReports.isEmpty()) Text(stringResource(R.string.no_reports_matching_filters))
        state.filteredReports.forEach { report -> ReportCard(report) {
            TextButton(onClick = { detail(report.folio) }) { Text(stringResource(R.string.view_dossier)) }
            if (report.autorEmail == state.session?.email) Row {
                TextButton(onClick = { edit(report.folio) }, enabled = !state.busy) { Text(stringResource(R.string.edit_button)) }
                TextButton(onClick = { deleteFolio = report.folio }, enabled = !state.busy) { Text(stringResource(R.string.delete_button)) }
            }
        } }
        TextButton(onClick = { confirmLogout = true }, enabled = !state.busy) { Text(stringResource(R.string.logout_button)) }
    }
    deleteFolio?.let { folio -> AlertDialog(onDismissRequest = { deleteFolio = null }, title = { Text(stringResource(R.string.delete_report_title, folio)) }, text = { Text(stringResource(R.string.delete_report_message)) }, confirmButton = { TextButton(onClick = { vm.eliminarReporte(folio); deleteFolio = null }) { Text(stringResource(R.string.delete_confirm_button)) } }, dismissButton = { TextButton(onClick = { deleteFolio = null }) { Text(stringResource(R.string.cancel_button)) } }) }
    if (confirmLogout) AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text(stringResource(R.string.logout_title)) }, text = { Text(stringResource(R.string.logout_message)) }, confirmButton = { TextButton(onClick = { confirmLogout = false; logout() }) { Text(stringResource(R.string.logout_confirm_button)) } }, dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel_button)) } })
}
