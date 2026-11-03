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
@Composable fun DetalleReporteScreen(vm: ReportesViewModel, folio: String, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val report = state.allReports.find { it.folio == folio }
    val history by remember(folio) { vm.history(folio) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val media by remember(folio) { vm.multimedia(folio) }.collectAsStateWithLifecycle(initialValue = emptyList())
    Page("Expediente $folio", back) {
        if (report == null) Text("Reporte no disponible.") else {
            ReportCard(report) { Text("Reportado por ${report.autorNombre}"); Text("Creado: ${dateLabel(report.fechaCreacion)}") }
            Evidence(report.fotoInicialUri, "Fotografía inicial")
            Text("Seguimiento", style = MaterialTheme.typography.titleLarge)
            Text("Actualizado: ${dateLabel(report.fechaActualizacion)} · Versión ${report.version}")
            report.observacionAdmin?.let { Text(it) }
            report.responsableAdmin?.let { Text("Responsable: $it") }
            if (report.estado == "RESUELTO") Evidence(report.evidenciaResueltoUri, "Evidencia de caso resuelto")
            Text("${media.size} evidencias guardadas", style = MaterialTheme.typography.bodySmall)
            Text("Historial del caso", style = MaterialTheme.typography.titleLarge)
            Text("${dateLabel(report.fechaCreacion)} · Reporte creado")
            history.forEach { event -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(event.titulo, style = MaterialTheme.typography.titleSmall); Text(event.mensaje); Text(dateLabel(event.fecha), style = MaterialTheme.typography.bodySmall) } } }
        }
    }
}
