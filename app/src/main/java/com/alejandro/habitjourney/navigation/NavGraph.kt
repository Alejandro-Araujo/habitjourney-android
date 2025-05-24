package com.alejandro.habitjourney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.alejandro.habitjourney.features.user.presentation.ui.LoginScreen
import com.alejandro.habitjourney.features.user.presentation.ui.RegisterScreen
import androidx.compose.ui.Modifier // Importar Modifier
import androidx.compose.foundation.layout.fillMaxSize // Importar fillMaxSize
import androidx.compose.foundation.layout.padding // Importar padding
import androidx.compose.foundation.layout.Arrangement // Importar Arrangement
import androidx.compose.foundation.layout.Column // Importar Column
import androidx.compose.foundation.layout.Spacer // Importar Spacer
import androidx.compose.foundation.layout.height // Importar height
import androidx.compose.material3.Button // Importar Button
import androidx.compose.material3.MaterialTheme // Importar MaterialTheme
import androidx.compose.material3.OutlinedButton // Importar OutlinedButton
import androidx.compose.material3.Text // Importar Text
import androidx.compose.ui.Alignment // Importar Alignment
import androidx.compose.ui.unit.dp // Importar dp

// Asegúrate de que tus pantallas reales estén importadas o comentadas si no existen aún
// import com.alejandro.habitjourney.features.dashboard.presentation.ui.DashboardScreen
// import com.alejandro.habitjourney.features.habit.presentation.ui.*
// import com.alejandro.habitjourney.features.task.presentation.ui.*
// import com.alejandro.habitjourney.features.note.presentation.ui.*
// import com.alejandro.habitjourney.features.achievement.presentation.ui.*
// import com.alejandro.habitjourney.features.settings.presentation.ui.*
// import com.alejandro.habitjourney.features.progress.presentation.ui.*

/**
 * Composable principal que maneja toda la navegación de la aplicación.
 *
 * @param navController Controlador de navegación
 * @param startDestination Destino inicial (calculado en HabitJourneyApp)
 * @param modifier Modificador para aplicar a NavHost
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String, // Ahora el startDestination ya viene decidido
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination, // Usa el startDestination que ya viene
        modifier = modifier // Aplica el modificador
    ) {

        // ==========================================
        // AUTHENTICATION GRAPH
        // ==========================================

        composable(Screen.Login.route) { backStackEntry ->
            // Inyectar el AuthViewModel aquí para poder usar sus funciones de estado
            val authViewModel = hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(backStackEntry) // Especificar el paquete completo
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    authViewModel.onLoginSuccess() // Notificar al AuthViewModel del login exitoso
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Register.route) { backStackEntry ->
            val authViewModel = hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(backStackEntry) // Especificar el paquete completo
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterSuccess = {
                    authViewModel.onLoginSuccess() // Asumimos que un registro exitoso implica un login
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ==========================================
        // MAIN APP GRAPH
        // ==========================================

        composable(Screen.Dashboard.route) { backStackEntry ->
            val authViewModel = hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(backStackEntry) // Especificar el paquete completo
            // TODO: Implementar DashboardScreen
            // DashboardScreen(
            //     onNavigateToHabits = { navController.navigate(Screen.HabitList.route) },
            //     onNavigateToTasks = { navController.navigate(Screen.TaskList.route) },
            //     onNavigateToNotes = { navController.navigate(Screen.NoteList.route) },
            //     onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
            //     onLogout = {
            //         authViewModel.logout()
            //         navController.navigate(Screen.Login.route) {
            //             popUpTo(0) { inclusive = true }
            //         }
            //     }
            // )

            // Placeholder temporal
            PlaceholderScreen(
                title = "Dashboard",
                onNavigateToHabits = { navController.navigate(Screen.HabitList.route) },
                onLogout = {
                    authViewModel.logout() // Llamar a logout del AuthViewModel
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // HABIT MANAGEMENT GRAPH
        // ==========================================

        composable(Screen.HabitList.route) {
            // TODO: Implementar HabitListScreen
            // HabitListScreen(
            //     onNavigateToHabitDetail = { habitId ->
            //         navController.navigate(Screen.HabitDetail.createRoute(habitId))
            //     },
            //     onNavigateToCreateHabit = {
            //         navController.navigate(Screen.CreateEditHabit.createRoute())
            //     },
            //     onNavigateBack = { navController.popBackStack() }
            // )

            // Placeholder temporal
            PlaceholderScreen(
                title = "Habit List",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L // Añadido defaultValue por seguridad
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L // Usar -1L si es nulo

            // TODO: Implementar HabitDetailScreen
            // HabitDetailScreen(
            //     habitId = habitId,
            //     onNavigateToEdit = {
            //         navController.navigate(Screen.CreateEditHabit.createRoute(habitId))
            //     },
            //     onNavigateBack = { navController.popBackStack() }
            // )

            // Placeholder temporal
            PlaceholderScreen(
                title = "Habit Detail #$habitId",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateEditHabit.route,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L // Asegura que el valor por defecto sea -1L
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId")
            val isEditing = habitId != null && habitId != -1L

            // TODO: Implementar CreateEditHabitScreen
            // CreateEditHabitScreen(
            //     habitId = if (isEditing) habitId else null,
            //     onHabitSaved = { navController.popBackStack() },
            //     onNavigateBack = { navController.popBackStack() }
            // )

            // Placeholder temporal
            PlaceholderScreen(
                title = if (isEditing) "Edit Habit #$habitId" else "Create Habit",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // TASK MANAGEMENT GRAPH
        // ==========================================

        composable(Screen.TaskList.route) {
            // TODO: Implementar TaskListScreen
            PlaceholderScreen(
                title = "Task List",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // NOTE MANAGEMENT GRAPH
        // ==========================================

        composable(Screen.NoteList.route) {
            // TODO: Implementar NoteListScreen
            PlaceholderScreen(
                title = "Note List",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // PROGRESS GRAPH
        // ==========================================

        composable(Screen.Progress.route) {
            // TODO: Implementar ProgressScreen
            PlaceholderScreen(
                title = "Progress",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // ACHIEVEMENT GRAPH
        // ==========================================

        composable(Screen.Achievements.route) {
            // TODO: Implementar AchievementScreen
            PlaceholderScreen(
                title = "Achievements",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // SETTINGS GRAPH
        // ==========================================

        composable(Screen.Settings.route) {
            // TODO: Implementar SettingsScreen
            PlaceholderScreen(
                title = "Settings",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Pantalla placeholder temporal para mostrar mientras se implementan las pantallas reales
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToHabits: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        if (onNavigateBack != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateBack
            ) {
                Text("Back")
            }
        }

        if (onNavigateToHabits != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToHabits
            ) {
                Text("Go to Habits")
            }
        }

        if (onLogout != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLogout
            ) {
                Text("Logout")
            }
        }
    }
}