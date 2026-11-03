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
@Composable fun BorradoresScreen(vm: ReportesViewModel, resume: (Int) -> Unit, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page("Mis borradores", back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        Text("Se guardan en este dispositivo y se eliminan al cerrar sesión.")
        if (state.drafts.isEmpty()) Text("Aún no tienes borradores.")
        state.drafts.forEach { draft -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(Department.fromCode(draft.departamento).displayName, style = MaterialTheme.typography.titleMedium)
            Text(draft.descripcion.ifBlank { "Sin descripción" }); Text(draft.ubicacion.ifBlank { "Sin ubicación" }); Text(dateLabel(draft.actualizadoEn))
            TextButton(onClick = { resume(draft.id) }, enabled = !state.busy) { Text("Retomar / editar") }
            TextButton(onClick = { vm.crearOEditarReporte(null, draft.id, draft.departamento, draft.descripcion, draft.ubicacion, draft.fotoLocalUri, {}) }, enabled = !state.busy) { Text("Enviar reporte") }
            TextButton(onClick = { vm.eliminarBorrador(draft.id) }, enabled = !state.busy) { Text("Eliminar borrador") }
        } } }
    }
}
