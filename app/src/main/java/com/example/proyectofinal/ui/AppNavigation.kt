package com.example.proyectofinal.ui
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.proyectofinal.model.UserRole
import com.example.proyectofinal.ui.auth.*
import com.example.proyectofinal.ui.reportes.*
import com.example.proyectofinal.ui.admin.*

import androidx.compose.ui.res.stringResource
import com.example.proyectofinal.R

@Composable fun AppNavigation(factory: ViewModelProvider.Factory, windowSize: WindowSizeClass) {
    val auth: AuthViewModel = viewModel(factory = factory)
    val reports: ReportesViewModel = viewModel(factory = factory)
    val admin: AdminViewModel = viewModel(factory = factory)
    val state by auth.uiState.collectAsStateWithLifecycle()
    val session = state.session
    LaunchedEffect(Unit) { auth.restoreSession() }
    LaunchedEffect(session) { reports.bind(session); admin.bind(session) }
    key(session?.uid, session?.role) {
        val nav = rememberNavController()
        val back: () -> Unit = { nav.popBackStack() }
        val detail: (String) -> Unit = { nav.navigate("detalle_reporte/$it") }
        val start = when(session?.role) { UserRole.ADMIN -> "admin_dashboard"; UserRole.CIUDADANO -> "listado_reportes"; null -> "login" }
        
        AdaptiveLayout(
            windowSize = windowSize,
            navigation = {
                if (session != null) {
                    val navBackStackEntry by nav.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                        NavigationBar {
                            if (session.role == UserRole.CIUDADANO) {
                                NavigationBarItem(
                                    icon = { Text("📝") },
                                    label = { Text(stringResource(R.string.nav_reports)) },
                                    selected = currentRoute == "listado_reportes",
                                    onClick = { nav.navigate("listado_reportes") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationBarItem(
                                    icon = { Text("✏️") },
                                    label = { Text(stringResource(R.string.nav_drafts)) },
                                    selected = currentRoute == "borradores",
                                    onClick = { nav.navigate("borradores") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationBarItem(
                                    icon = { Text("🔔") },
                                    label = { Text(stringResource(R.string.nav_notifications)) },
                                    selected = currentRoute == "notificaciones",
                                    onClick = { nav.navigate("notificaciones") { launchSingleTop = true; restoreState = true } }
                                )
                            } else {
                                NavigationBarItem(
                                    icon = { Text("📊") },
                                    label = { Text(stringResource(R.string.nav_dashboard)) },
                                    selected = currentRoute == "admin_dashboard",
                                    onClick = { nav.navigate("admin_dashboard") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationBarItem(
                                    icon = { Text("📥") },
                                    label = { Text(stringResource(R.string.nav_inbox)) },
                                    selected = currentRoute == "admin_bandeja",
                                    onClick = { nav.navigate("admin_bandeja") { launchSingleTop = true; restoreState = true } }
                                )
                            }
                        }
                    } else {
                        NavigationRail {
                            if (session.role == UserRole.CIUDADANO) {
                                NavigationRailItem(
                                    icon = { Text("📝") },
                                    label = { Text(stringResource(R.string.nav_reports)) },
                                    selected = currentRoute == "listado_reportes",
                                    onClick = { nav.navigate("listado_reportes") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationRailItem(
                                    icon = { Text("✏️") },
                                    label = { Text(stringResource(R.string.nav_drafts)) },
                                    selected = currentRoute == "borradores",
                                    onClick = { nav.navigate("borradores") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationRailItem(
                                    icon = { Text("🔔") },
                                    label = { Text(stringResource(R.string.nav_notifications)) },
                                    selected = currentRoute == "notificaciones",
                                    onClick = { nav.navigate("notificaciones") { launchSingleTop = true; restoreState = true } }
                                )
                            } else {
                                NavigationRailItem(
                                    icon = { Text("📊") },
                                    label = { Text(stringResource(R.string.nav_dashboard)) },
                                    selected = currentRoute == "admin_dashboard",
                                    onClick = { nav.navigate("admin_dashboard") { launchSingleTop = true; restoreState = true } }
                                )
                                NavigationRailItem(
                                    icon = { Text("📥") },
                                    label = { Text(stringResource(R.string.nav_inbox)) },
                                    selected = currentRoute == "admin_bandeja",
                                    onClick = { nav.navigate("admin_bandeja") { launchSingleTop = true; restoreState = true } }
                                )
                            }
                        }
                    }
                }
            }
        ) {
            NavHost(navController = nav, startDestination = start) {
                if (session == null) {
                    composable("login") { LoginScreen(auth, { auth.clearMessage(); nav.navigate("register") }, { auth.clearMessage(); nav.navigate("recovery") }) }
                    composable("register") { RegisterScreen(auth, back) }
                    composable("recovery") { RecoveryScreen(auth, back) }
                } else {
                    composable("detalle_reporte/{folio}") { entry -> DetalleReporteScreen(reports, requireNotNull(entry.arguments?.getString("folio")), back) }
                    if (session.role == UserRole.CIUDADANO) {
                        composable("listado_reportes") { ListadoReportesScreen(reports, { nav.navigate("captura_incidencia") }, detail, { nav.navigate("captura_incidencia?folio=$it") }, auth::logout) }
                        composable("captura_incidencia?folio={folio}&draftId={draftId}", arguments = listOf(navArgument("folio") { type = NavType.StringType; nullable = true; defaultValue = null }, navArgument("draftId") { type = NavType.IntType; defaultValue = -1 })) { entry ->
                            CapturaIncidenciaScreen(reports, entry.arguments?.getString("folio"), entry.arguments?.getInt("draftId")?.takeIf { it > 0 }, back)
                        }
                        composable("borradores") { BorradoresScreen(reports, { nav.navigate("captura_incidencia?draftId=$it") }, back) }
                        composable("notificaciones") { NotificacionesScreen(reports, detail, back) }
                    } else {
                        composable("admin_dashboard") { AdminDashboardScreen(admin, auth::logout) }
                        composable("admin_bandeja") { AdminBandejaScreen(admin, detail, { nav.navigate("admin_resolver/$it") }, back) }
                        composable("admin_resolver/{folio}") { entry -> AdminResolverScreen(admin, requireNotNull(entry.arguments?.getString("folio")), session.email, back) }
                    }
                }
            }
        }
    }
}
