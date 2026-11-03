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
@Composable fun AdminDashboardScreen(vm: AdminViewModel, inbox: () -> Unit, logout: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page("Panel de administración") {
        Text("Panorama de la comunidad", style = MaterialTheme.typography.headlineSmall)
        Feedback(state.busy, state.message, vm::clearMessage)
        listOf("Total de reportes" to state.total, "Pendientes" to state.pendientes, "En proceso" to state.enProceso, "Resueltos" to state.resueltos).forEach { (label, count) ->
            ElevatedCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(count.toString(), style = MaterialTheme.typography.titleLarge) } }
        }
        Text("Atención: ${"%.1f".format(state.porcentajeAtencion)}% resueltos")
        LinearProgressIndicator(progress = { state.porcentajeAtencion / 100 }, modifier = Modifier.fillMaxWidth())
        Button(onClick = inbox, modifier = Modifier.fillMaxWidth()) { Text("Abrir bandeja de incidentes") }
        Text("Indicadores por departamento", style = MaterialTheme.typography.titleLarge)
        state.departments.forEach { kpi -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(kpi.department.departmentArea, style = MaterialTheme.typography.titleMedium)
            Text("${kpi.department.displayName} · ${kpi.total} reportes")
            Text("${kpi.pendientes} pendientes · ${kpi.enProceso} en proceso · ${kpi.resueltos} resueltos")
            LinearProgressIndicator(progress = { if (kpi.total == 0) 0f else kpi.resueltos.toFloat() / kpi.total }, modifier = Modifier.fillMaxWidth())
        } } }
        TextButton(onClick = logout, enabled = !state.busy) { Text("Cerrar sesión") }
    }
}
