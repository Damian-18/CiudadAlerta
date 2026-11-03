package com.example.proyectofinal
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class DemoFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun tap(text: String) { val node = compose.onNodeWithText(text); runCatching { node.performScrollTo() }; node.performClick() }
    private fun awaitText(text: String) { compose.waitUntil(15000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
    @Test fun citizenDraftCreateEditDeleteAndAdminResolve() {
        tap("Demo Ciudadano")
        awaitText("Incidentes de la comunidad")
        awaitText("CA-1001")
        tap("Reportar incidencia")
        compose.onNodeWithText("Descripción").performTextInput("Bache de prueba de integración en avenida central")
        compose.onNodeWithText("Ubicación escrita").performTextInput("Avenida Central número 123")
        tap("Usar foto de prueba simulada")
        tap("Guardar borrador local")
        awaitText("Incidentes de la comunidad")
        tap("Borradores (1)")
        awaitText("Mis borradores")
        tap("Retomar / editar")
        tap("Enviar reporte")
        awaitText("Mis borradores")
        compose.onNodeWithText("Aún no tienes borradores.").assertExists()
        tap("Volver")
        awaitText("Incidentes de la comunidad")
        compose.onAllNodesWithText("Editar")[0].performScrollTo().performClick()
        compose.onNodeWithText("Descripción").performTextReplacement("Bache de prueba editado en avenida central")
        tap("Guardar cambios")
        awaitText("Incidentes de la comunidad")
        compose.onNodeWithText("Bache de prueba editado en avenida central").assertExists()
        tap("Cerrar sesión")
        tap("Salir")
        awaitText("Demo Admin")
        tap("Demo Admin")
        awaitText("Panel de administración")
        tap("Abrir bandeja de incidentes")
        awaitText("Bandeja administrativa")
        compose.onAllNodesWithText("Gestionar caso")[0].performScrollTo().performClick()
        compose.onNodeWithText("Observación obligatoria").performTextInput("Trabajo finalizado y verificado")
        tap("Cambiar a En proceso")
        awaitText("Bandeja administrativa")
        compose.onAllNodesWithText("Gestionar caso")[0].performScrollTo().performClick()
        compose.onNodeWithText("Observación obligatoria").performTextInput("Reparación completada y verificada")
        tap("Usar foto de prueba simulada")
        tap("Cambiar a Resuelto")
        awaitText("Bandeja administrativa")
        tap("Volver")
        awaitText("Panel de administración")
        tap("Cerrar sesión")
        awaitText("Demo Ciudadano")
        tap("Demo Ciudadano")
        awaitText("Incidentes de la comunidad")
        awaitText("Bache de prueba editado en avenida central")
        compose.onAllNodesWithText("Ver expediente")[0].performScrollTo().performClick()
        awaitText("Evidencia de caso resuelto")
        compose.onAllNodesWithText("Reparación completada y verificada", substring = true)[0].assertExists()
        tap("Volver")
        compose.onAllNodesWithText("Eliminar")[0].performScrollTo().performClick()
        compose.onAllNodesWithText("Eliminar").onLast().performClick()
        awaitText("Reporte eliminado")
    }
}
