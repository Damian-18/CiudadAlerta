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
import com.example.proyectofinal.ui.*
@Composable fun ListadoReportesScreen(vm: ReportesViewModel, newReport: () -> Unit, detail: (String) -> Unit, edit: (String) -> Unit, drafts: () -> Unit, notifications: () -> Unit, logout: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var deleteFolio by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    Page("Incidentes de la comunidad") {
        Text("Hola, ${state.session?.nombre.orEmpty()}", style = MaterialTheme.typography.titleLarge)
        Feedback(state.busy, state.message, vm::clearMessage)
        Button(onClick = newReport, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Reportar incidencia") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = drafts) { Text("Borradores (${state.drafts.size})") }
            TextButton(onClick = notifications) { Text("Avisos (${state.notifications.count { !it.leida }})") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (state.showOnlyMyReports) "Mis Reportes" else "Todos los incidentes")
            Switch(checked = state.showOnlyMyReports, onCheckedChange = vm::onlyMine)
        }
        Filters(state.selectedDepartment, state.selectedStatus, vm::department, vm::status)
        OutlinedButton(onClick = vm::syncRemoteReports, enabled = !state.busy) { Text("Actualizar reportes") }
        Text("${state.filteredReports.size} incidentes", style = MaterialTheme.typography.titleMedium)
        if (state.filteredReports.isEmpty()) Text("No hay reportes que coincidan con los filtros.")
        state.filteredReports.forEach { report -> ReportCard(report) {
            TextButton(onClick = { detail(report.folio) }) { Text("Ver expediente") }
            if (report.autorEmail == state.session?.email) Row {
                TextButton(onClick = { edit(report.folio) }, enabled = !state.busy) { Text("Editar") }
                TextButton(onClick = { deleteFolio = report.folio }, enabled = !state.busy) { Text("Eliminar") }
            }
        } }
        TextButton(onClick = { confirmLogout = true }, enabled = !state.busy) { Text("Cerrar sesión") }
    }
    deleteFolio?.let { folio -> AlertDialog(onDismissRequest = { deleteFolio = null }, title = { Text("Eliminar $folio") }, text = { Text("Se eliminarán el reporte y sus evidencias y avisos asociados.") }, confirmButton = { TextButton(onClick = { vm.eliminarReporte(folio); deleteFolio = null }) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { deleteFolio = null }) { Text("Cancelar") } }) }
    if (confirmLogout) AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text("Cerrar sesión") }, text = { Text("Se borrarán tus borradores locales. Los reportes enviados se conservarán.") }, confirmButton = { TextButton(onClick = { confirmLogout = false; logout() }) { Text("Salir") } }, dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } })
}
