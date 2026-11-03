package com.example.proyectofinal.ui
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import com.example.proyectofinal.model.UserRole
import com.example.proyectofinal.ui.auth.*
import com.example.proyectofinal.ui.reportes.*
import com.example.proyectofinal.ui.admin.*

@Composable fun AppNavigation(factory: ViewModelProvider.Factory) {
    val auth: AuthViewModel = viewModel(factory = factory)
    val reports: ReportesViewModel = viewModel(factory = factory)
    val admin: AdminViewModel = viewModel(factory = factory)
    val state by auth.uiState.collectAsStateWithLifecycle()
    val session = state.session
    LaunchedEffect(session) { reports.bind(session); admin.bind(session) }
    key(session?.uid, session?.role) {
        val nav = rememberNavController()
        val back: () -> Unit = { nav.popBackStack() }
        val detail: (String) -> Unit = { nav.navigate("detalle_reporte/$it") }
        val start = when(session?.role) { UserRole.ADMIN -> "admin_dashboard"; UserRole.CIUDADANO -> "listado_reportes"; null -> "login" }
        NavHost(navController = nav, startDestination = start) {
            if (session == null) {
                composable("login") { LoginScreen(auth, { auth.clearMessage(); nav.navigate("register") }, { auth.clearMessage(); nav.navigate("recovery") }) }
                composable("register") { RegisterScreen(auth, back) }
                composable("recovery") { RecoveryScreen(auth, back) }
            } else {
                composable("detalle_reporte/{folio}") { entry -> DetalleReporteScreen(reports, requireNotNull(entry.arguments?.getString("folio")), back) }
                if (session.role == UserRole.CIUDADANO) {
                    composable("listado_reportes") { ListadoReportesScreen(reports, { nav.navigate("captura_incidencia") }, detail, { nav.navigate("captura_incidencia?folio=$it") }, { nav.navigate("borradores") }, { nav.navigate("notificaciones") }, auth::logout) }
                    composable("captura_incidencia?folio={folio}&draftId={draftId}", arguments = listOf(navArgument("folio") { type = NavType.StringType; nullable = true; defaultValue = null }, navArgument("draftId") { type = NavType.IntType; defaultValue = -1 })) { entry ->
                        CapturaIncidenciaScreen(reports, entry.arguments?.getString("folio"), entry.arguments?.getInt("draftId")?.takeIf { it > 0 }, back)
                    }
                    composable("borradores") { BorradoresScreen(reports, { nav.navigate("captura_incidencia?draftId=$it") }, back) }
                    composable("notificaciones") { NotificacionesScreen(reports, detail, back) }
                } else {
                    composable("admin_dashboard") { AdminDashboardScreen(admin, { nav.navigate("admin_bandeja") }, auth::logout) }
                    composable("admin_bandeja") { AdminBandejaScreen(admin, detail, { nav.navigate("admin_resolver/$it") }, back) }
                    composable("admin_resolver/{folio}") { entry -> AdminResolverScreen(admin, requireNotNull(entry.arguments?.getString("folio")), session.email, back) }
                }
            }
        }
    }
}
