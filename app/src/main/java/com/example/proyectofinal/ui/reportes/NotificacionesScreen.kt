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
@Composable fun NotificacionesScreen(vm: ReportesViewModel, detail: (String) -> Unit, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page("Notificaciones", back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        if (state.notifications.isEmpty()) Text("No tienes notificaciones.")
        state.notifications.forEach { notification -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (notification.leida) "Leída" else "Nueva", color = MaterialTheme.colorScheme.primary)
            Text(notification.titulo, style = MaterialTheme.typography.titleMedium); Text(notification.mensaje); Text(dateLabel(notification.fecha))
            TextButton(onClick = { vm.marcarNotificacionLeida(notification.id); detail(notification.reporteFolio) }) { Text("Ver reporte") }
            if (!notification.leida) TextButton(onClick = { vm.marcarNotificacionLeida(notification.id) }, enabled = !state.busy) { Text("Marcar como leída") }
        } } }
    }
}
