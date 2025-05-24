package com.alejandro.habitjourney.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel // Importar AuthViewModel

/**
 * Composable principal de la aplicación que maneja la navegación
 * y la estructura general (con o sin BottomNavigation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitJourneyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Inyectar el AuthViewModel al nivel más alto de la aplicación
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isLoadingAuth by authViewModel.isLoading.collectAsState()

    // Determinar si mostrar BottomNavigation
    // Solo se muestra si el usuario está logueado Y la ruta actual está en las rutas de bottomNav
    val showBottomNav = isLoggedIn && (currentDestination?.route in NavigationRoutes.bottomNavRoutes)

    // Decidir el destino inicial basándose en el estado de autenticación
    val startDestination = if (isLoadingAuth) {
        // Mientras se carga el estado de autenticación, puedes mostrar un SplashScreen
        // O simplemente dejar que la pantalla de login/dashboard se decida después
        // Por ahora, lo dejamos en login por defecto si no hay un dashboard cargado previamente.
        Screen.Login.route
    } else if (isLoggedIn) {
        Screen.Dashboard.route
    } else {
        Screen.Login.route
    }


    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                HabitJourneyBottomNavigation(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { innerPadding ->
        // Pasar el padding a NavGraph para que el contenido no se solape con la BottomBar
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Bottom Navigation Bar para las pantallas principales
 */
@Composable
private fun HabitJourneyBottomNavigation(
    navController: androidx.navigation.NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.contentDescriptionResId) // Usar stringResource
                    )
                },
                label = { Text(stringResource(id = item.titleResId)) }, // Usar stringResource
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Data class para los items del bottom navigation
 */
private data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val titleResId: Int, // Referencia al ID del string
    val contentDescriptionResId: Int // Referencia al ID del string para accesibilidad
)

/**
 * Lista de items para el bottom navigation
 */
private val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        icon = Icons.Default.Home,
        titleResId = R.string.nav_dashboard,
        contentDescriptionResId = R.string.nav_dashboard // Puedes crear un content_description específico si lo deseas
    ),
    BottomNavItem(
        route = Screen.HabitList.route,
        icon = Icons.Default.CheckCircle,
        titleResId = R.string.nav_habits,
        contentDescriptionResId = R.string.nav_habits
    ),
    BottomNavItem(
        route = Screen.TaskList.route,
        icon = Icons.Default.Task,
        titleResId = R.string.nav_tasks,
        contentDescriptionResId = R.string.nav_tasks
    ),
    BottomNavItem(
        route = Screen.NoteList.route,
        icon = Icons.Default.Note,
        titleResId = R.string.nav_notes,
        contentDescriptionResId = R.string.nav_notes
    ),
    BottomNavItem(
        route = Screen.Progress.route,
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        titleResId = R.string.nav_progress,
        contentDescriptionResId = R.string.nav_progress
    )
)