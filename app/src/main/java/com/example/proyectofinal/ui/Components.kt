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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import coil.compose.AsyncImage
import com.example.proyectofinal.data.local.entity.ReportEntity
import com.example.proyectofinal.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun Page(title: String, back: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { if (back != null) TextButton(onClick = back) { Text(stringResource(R.string.btn_back)) } }) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter
        ) {
            Column(
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
fun AdaptiveLayout(
    windowSize: WindowSizeClass,
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) { content() }
            navigation()
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            navigation()
            Box(Modifier.weight(1f)) { content() }
        }
    }
}
@Composable fun Feedback(busy: Boolean, message: String?, clear: () -> Unit) {
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (message != null) Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text(message); TextButton(onClick = clear) { Text(stringResource(R.string.btn_understood)) } } }
}
@Composable fun <T> Choice(label: String, selected: T, options: List<T>, text: @Composable (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: ${text(selected)}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { value -> DropdownMenuItem(text = { Text(text(value)) }, onClick = { onSelect(value); expanded = false }) }
        }
    }
}
@Composable fun Filters(department: Department?, status: ReportStatus?, onDepartment: (Department?) -> Unit, onStatus: (ReportStatus?) -> Unit) {
    Choice(stringResource(R.string.department_label), department, listOf(null) + Department.entries, { it?.displayNameRes?.let { res -> stringResource(res) } ?: stringResource(R.string.filter_all) }, onDepartment)
    Choice(stringResource(R.string.status_label), status, listOf(null) + ReportStatus.entries, { it?.displayNameRes?.let { res -> stringResource(res) } ?: stringResource(R.string.filter_all) }, onStatus)
}
@Composable fun ReportCard(report: ReportEntity, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(report.folio, style = MaterialTheme.typography.titleMedium)
        Text("${stringResource(Department.fromCode(report.departamento).displayNameRes)} · ${stringResource(ReportStatus.fromCode(report.estado).displayNameRes)}", color = MaterialTheme.colorScheme.primary)
        Text(report.descripcion)
        Text(report.ubicacion, style = MaterialTheme.typography.bodySmall)
        content()
    } }
}
@Composable fun Evidence(uri: String?, label: String) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    if (uri.isNullOrBlank()) Text(stringResource(R.string.no_photo_attached)) else {
        var failed by remember(uri) { mutableStateOf(false) }
        AsyncImage(model = uri, contentDescription = label, modifier = Modifier.fillMaxWidth().height(210.dp), contentScale = ContentScale.Fit, onError = { failed = true })
        if (failed) Text(stringResource(R.string.image_open_error))
        if (uri.startsWith("file:///android_asset/")) Text(stringResource(R.string.simulated_test_image), style = MaterialTheme.typography.labelSmall)
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
            } catch (e: CancellationException) { throw e } catch (e: Exception) { error = context.getString(R.string.image_save_error, e.message) } finally { busy = false }
        }
    }
    Evidence(uri, if (resolved) stringResource(R.string.resolved_evidence) else stringResource(R.string.incident_photo))
    OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.select_photo_button)) }
    if (uri != null) TextButton(onClick = { onChange(null) }, enabled = !busy) { Text(stringResource(R.string.remove_photo_button)) }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
fun dateLabel(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
