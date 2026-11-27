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
@Composable fun NotificacionesScreen(vm: ReportesViewModel, detail: (String) -> Unit, back: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    Page(stringResource(R.string.notifications_title), back) {
        Feedback(state.busy, state.message, vm::clearMessage)
        if (state.notifications.isEmpty()) Text(stringResource(R.string.no_notifications))
        state.notifications.forEach { notification -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (notification.leida) stringResource(R.string.notification_read) else stringResource(R.string.notification_new), color = MaterialTheme.colorScheme.primary)
            Text(notification.titulo, style = MaterialTheme.typography.titleMedium); Text(notification.mensaje); Text(dateLabel(notification.fecha))
            TextButton(onClick = { vm.marcarNotificacionLeida(notification.id); detail(notification.reporteFolio) }) { Text(stringResource(R.string.view_report_button)) }
            if (!notification.leida) TextButton(onClick = { vm.marcarNotificacionLeida(notification.id) }, enabled = !state.busy) { Text(stringResource(R.string.mark_as_read_button)) }
        } } }
    }
}
