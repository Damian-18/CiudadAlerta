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
@Composable fun BorradoresScreen(vm: ReportesViewModel, resume: (Int) -> Unit, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page(stringResource(R.string.my_drafts_title), back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        Text(stringResource(R.string.drafts_info))
        if (state.drafts.isEmpty()) Text(stringResource(R.string.no_drafts_yet))
        state.drafts.forEach { draft -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Department.fromCode(draft.departamento).displayNameRes), style = MaterialTheme.typography.titleMedium)
            Text(draft.descripcion.ifBlank { stringResource(R.string.no_description) }); Text(draft.ubicacion.ifBlank { stringResource(R.string.no_location) }); Text(dateLabel(draft.actualizadoEn))
            TextButton(onClick = { resume(draft.id) }, enabled = !state.busy) { Text(stringResource(R.string.resume_edit_button)) }
            TextButton(onClick = { vm.crearOEditarReporte(null, draft.id, draft.departamento, draft.descripcion, draft.ubicacion, draft.fotoLocalUri, {}) }, enabled = !state.busy) { Text(stringResource(R.string.submit_report_button)) }
            TextButton(onClick = { vm.eliminarBorrador(draft.id) }, enabled = !state.busy) { Text(stringResource(R.string.delete_draft_button)) }
        } } }
    }
}
