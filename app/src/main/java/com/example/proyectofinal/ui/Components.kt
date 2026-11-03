package com.example.proyectofinal.ui
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyectofinal.data.local.DemoData
import com.example.proyectofinal.data.local.entity.ReportEntity
import com.example.proyectofinal.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun Page(title: String, back: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { if (back != null) TextButton(onClick = back) { Text("Volver") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}
@Composable fun Feedback(busy: Boolean, message: String?, clear: () -> Unit) {
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (message != null) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(message); TextButton(onClick = clear) { Text("Entendido") } } }
}
@Composable fun <T> Choice(label: String, selected: T, options: List<T>, text: (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: ${text(selected)}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { value -> DropdownMenuItem(text = { Text(text(value)) }, onClick = { onSelect(value); expanded = false }) }
        }
    }
}
@Composable fun Filters(department: Department?, status: ReportStatus?, onDepartment: (Department?) -> Unit, onStatus: (ReportStatus?) -> Unit) {
    Choice("Departamento", department, listOf(null) + Department.entries, { it?.displayName ?: "Todos" }, onDepartment)
    Choice("Estado", status, listOf(null) + ReportStatus.entries, { it?.displayName ?: "Todos" }, onStatus)
}
@Composable fun ReportCard(report: ReportEntity, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(report.folio, style = MaterialTheme.typography.titleMedium)
        Text("${Department.fromCode(report.departamento).displayName} · ${ReportStatus.fromCode(report.estado).displayName}", color = MaterialTheme.colorScheme.primary)
        Text(report.descripcion)
        Text(report.ubicacion, style = MaterialTheme.typography.bodySmall)
        content()
    } }
}
@Composable fun Evidence(uri: String?, label: String) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    if (uri.isNullOrBlank()) Text("Sin fotografía adjunta") else {
        var failed by remember(uri) { mutableStateOf(false) }
        AsyncImage(model = uri, contentDescription = label, modifier = Modifier.fillMaxWidth().height(210.dp), contentScale = ContentScale.Fit, onError = { failed = true })
        if (failed) Text("No se pudo abrir la imagen. Selecciona una fotografía disponible.")
        if (uri.startsWith("file:///android_asset/")) Text("Imagen de prueba simulada", style = MaterialTheme.typography.labelSmall)
    }
}
@Composable fun PhotoField(uri: String?, resolved: Boolean = false, onChange: (String?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        if (picked != null) scope.launch {
            busy = true; error = null
            try {
                val copy = withContext(Dispatchers.IO) {
                    val directory = File(context.filesDir, "evidence").apply { mkdirs() }
                    val target = File.createTempFile("photo_", ".img", directory)
                    try {
                        checkNotNull(context.contentResolver.openInputStream(picked)).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
                        Uri.fromFile(target).toString()
                    } catch (e: Exception) { target.delete(); throw e }
                }
                onChange(copy)
            } catch (e: CancellationException) { throw e } catch (e: Exception) { error = "No se pudo guardar la imagen: ${e.message}" } finally { busy = false }
        }
    }
    Evidence(uri, if (resolved) "Evidencia de caso resuelto" else "Fotografía del incidente")
    OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar fotografía") }
    TextButton(onClick = { onChange(if (resolved) DemoData.RESOLVED_PHOTO else DemoData.PHOTO) }, enabled = !busy) { Text("Usar foto de prueba simulada") }
    if (uri != null) TextButton(onClick = { onChange(null) }, enabled = !busy) { Text("Quitar foto") }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
fun dateLabel(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
