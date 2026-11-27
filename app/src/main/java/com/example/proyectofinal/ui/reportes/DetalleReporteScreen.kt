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
@Composable fun DetalleReporteScreen(vm: ReportesViewModel, folio: String, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val report = state.allReports.find { it.folio == folio }
    val history by remember(folio) { vm.history(folio) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val media by remember(folio) { vm.multimedia(folio) }.collectAsStateWithLifecycle(initialValue = emptyList())
    Page(stringResource(R.string.dossier_title, folio), back) {
        if (report == null) Text(stringResource(R.string.report_not_available)) else {
            ReportCard(report) { Text(stringResource(R.string.reported_by, report.autorNombre)); Text(stringResource(R.string.created_at, dateLabel(report.fechaCreacion))) }
            Evidence(report.fotoInicialUri, stringResource(R.string.initial_photo))
            Text(stringResource(R.string.tracking_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.updated_at_version, dateLabel(report.fechaActualizacion), report.version))
            report.observacionAdmin?.let { Text(it) }
            report.responsableAdmin?.let { Text(stringResource(R.string.responsible_admin, it)) }
            if (report.estado == "RESUELTO") Evidence(report.evidenciaResueltoUri, stringResource(R.string.resolved_evidence))
            Text(stringResource(R.string.saved_evidences, media.size), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.case_history_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.report_created_at, dateLabel(report.fechaCreacion)))
            history.forEach { event -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(event.titulo, style = MaterialTheme.typography.titleSmall); Text(event.mensaje); Text(dateLabel(event.fecha), style = MaterialTheme.typography.bodySmall) } } }
        }
    }
}
