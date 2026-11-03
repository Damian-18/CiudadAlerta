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
@Composable fun AdminBandejaScreen(vm: AdminViewModel, detail: (String) -> Unit, resolve: (String) -> Unit, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var department by rememberSaveable { mutableStateOf<Department?>(null) }
    var status by rememberSaveable { mutableStateOf<ReportStatus?>(null) }
    val reports = state.reports.filter { (department == null || it.departamento == department?.code) && (status == null || it.estado == status?.code) }
    Page("Bandeja administrativa", back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        Filters(department, status, { department = it }, { status = it })
        Text("${reports.size} incidentes")
        if (reports.isEmpty()) Text("No hay incidentes con estos filtros.")
        reports.forEach { report -> ReportCard(report) {
            TextButton(onClick = { detail(report.folio) }) { Text("Ver expediente") }
            if (report.estado != "RESUELTO") Button(onClick = { resolve(report.folio) }) { Text("Gestionar caso") }
        } }
    }
}
