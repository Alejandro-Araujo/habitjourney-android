package com.alejandro.habitjourney.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.alejandro.habitjourney.features.user.presentation.ui.LoginScreen
import com.alejandro.habitjourney.features.user.presentation.ui.RegisterScreen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.features.habit.presentation.ui.CreateEditHabitScreen
import com.alejandro.habitjourney.features.habit.presentation.ui.HabitDetailScreen
import com.alejandro.habitjourney.features.habit.presentation.ui.HabitListScreen
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.CreateEditHabitViewModel
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitDetailViewModel
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListViewModel


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
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ==========================================
        // AUTHENTICATION GRAPH
        // ==========================================

        composable(Screen.Login.route) { backStackEntry ->
            val authViewModel = hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(backStackEntry) // Especificar el paquete completo
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    authViewModel.onLoginSuccess()
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
                    authViewModel.onLoginSuccess()
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

        composable(Screen.HabitList.route) { backStackEntry ->
            val viewModel = hiltViewModel<HabitListViewModel>(backStackEntry)
            HabitListScreen(
                onNavigateToCreateHabit = {
                    navController.navigate(Screen.CreateEditHabit.createRoute())
                },
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
            val viewModel = hiltViewModel<HabitDetailViewModel>(backStackEntry)
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditHabit = { idToEdit ->
                    navController.navigate(Screen.CreateEditHabit.createRoute(idToEdit))
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.CreateEditHabit.route,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId")
            // Si el habitId es -1L, es una creación. Si es otro valor, es edición.
            val isEditing = habitId != null && habitId != -1L

            val viewModel = hiltViewModel<CreateEditHabitViewModel>(backStackEntry)
            CreateEditHabitScreen(
                habitId = if (isEditing) habitId else null, // Si es edición, pasa el ID, si no, pasa null.
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
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