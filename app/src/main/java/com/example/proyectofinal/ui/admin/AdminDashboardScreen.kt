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
@Composable fun AdminDashboardScreen(vm: AdminViewModel, logout: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page(stringResource(R.string.admin_dashboard_title)) {
        Text(stringResource(R.string.community_panorama), style = MaterialTheme.typography.headlineSmall)
        Feedback(state.busy, state.message, vm::clearMessage)
        listOf(
            stringResource(R.string.total_reports_kpi) to state.total, 
            stringResource(R.string.pending_kpi) to state.pendientes, 
            stringResource(R.string.in_process_kpi) to state.enProceso, 
            stringResource(R.string.resolved_kpi) to state.resueltos
        ).forEach { (label, count) ->
            ElevatedCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(count.toString(), style = MaterialTheme.typography.titleLarge) } }
        }
        Text(stringResource(R.string.attention_percent, "%.1f".format(state.porcentajeAtencion)))
        LinearProgressIndicator(progress = { state.porcentajeAtencion / 100 }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.indicators_by_department), style = MaterialTheme.typography.titleLarge)
        state.departments.forEach { kpi -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(kpi.department.departmentAreaRes), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.department_reports, stringResource(kpi.department.displayNameRes), kpi.total))
            Text(stringResource(R.string.kpi_stats, kpi.pendientes, kpi.enProceso, kpi.resueltos))
            LinearProgressIndicator(progress = { if (kpi.total == 0) 0f else kpi.resueltos.toFloat() / kpi.total }, modifier = Modifier.fillMaxWidth())
        } } }
        TextButton(onClick = logout, enabled = !state.busy) { Text(stringResource(R.string.logout_button)) }
    }
}
