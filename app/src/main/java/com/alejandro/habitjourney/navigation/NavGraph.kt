package com.alejandro.habitjourney.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.alejandro.habitjourney.features.user.presentation.screen.LoginScreen
import com.alejandro.habitjourney.features.user.presentation.screen.RegisterScreen
import androidx.compose.ui.Modifier
import com.alejandro.habitjourney.features.habit.presentation.screen.CreateEditHabitScreen
import com.alejandro.habitjourney.features.habit.presentation.screen.HabitDetailScreen
import com.alejandro.habitjourney.features.habit.presentation.screen.HabitListScreen
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.CreateEditHabitViewModel
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitDetailViewModel
import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListViewModel
import com.alejandro.habitjourney.features.settings.presentation.screen.ChangePasswordScreen
import com.alejandro.habitjourney.features.settings.presentation.screen.EditProfileScreen
import com.alejandro.habitjourney.features.settings.presentation.screen.LanguageSelectionScreen
import com.alejandro.habitjourney.features.settings.presentation.screen.SettingsScreen
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.ChangePasswordViewModel
import com.alejandro.habitjourney.features.settings.presentation.viewmodel.EditProfileViewModel
import com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel

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
            val authViewModel = hiltViewModel<AuthViewModel>(backStackEntry)

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
            val authViewModel =
                hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(
                    backStackEntry
                )
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
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
            val authViewModel =
                hiltViewModel<com.alejandro.habitjourney.features.user.presentation.viewmodel.AuthViewModel>(
                    backStackEntry
                )

            com.alejandro.habitjourney.features.dashboard.presentation.screen.DashboardScreen(
                onNavigateToHabits = {
                    navController.navigate(Screen.HabitList.route)
                },
                onNavigateToTasks = {
                    navController.navigate(Screen.TaskList.route)
                },
                onNavigateToNotes = {
                    navController.navigate(Screen.NoteList.route)
                },
                onNavigateToCreateHabit = {
                    navController.navigate(Screen.CreateEditHabit.createRoute())
                },
                onNavigateToCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onNavigateToCreateNote = {
                    navController.navigate(Screen.CreateNote.route)
                },
                onNavigateToHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
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
            val isEditing = habitId != null && habitId != -1L

            val viewModel = hiltViewModel<CreateEditHabitViewModel>(backStackEntry)
            CreateEditHabitScreen(
                habitId = if (isEditing) habitId else null,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // ==========================================
        // TASK MANAGEMENT GRAPH
        // ==========================================

        composable(Screen.TaskList.route) { backStackEntry ->
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.task.presentation.viewmodel.TaskListViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.task.presentation.screen.TaskListScreen(
                onNavigateToCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.task.presentation.viewmodel.TaskDetailsViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.task.presentation.screen.TaskDetailsScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { idToEdit ->
                    navController.navigate(Screen.EditTask.createRoute(idToEdit))
                },
                viewModel = viewModel
            )
        }

        composable(Screen.CreateTask.route) { backStackEntry ->
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.task.presentation.viewmodel.CreateEditTaskViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.task.presentation.screen.CreateEditTaskScreen(
                taskId = null,
                isReadOnly = false,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.task.presentation.viewmodel.CreateEditTaskViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.task.presentation.screen.CreateEditTaskScreen(
                taskId = taskId,
                isReadOnly = false,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // ==========================================
        // NOTE MANAGEMENT GRAPH
        // ==========================================

        composable(Screen.NoteList.route) { backStackEntry ->
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.note.presentation.viewmodel.NoteListViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.note.presentation.screen.NoteListScreen(
                onNavigateToCreateNote = {
                    navController.navigate(Screen.CreateNote.route)
                },
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.note.presentation.viewmodel.NoteDetailsViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.note.presentation.screen.NoteDetailsScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { idToEdit ->
                    navController.navigate(Screen.EditNote.createRoute(idToEdit))
                },
                viewModel = viewModel
            )
        }

        composable(Screen.CreateNote.route) { backStackEntry ->
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.note.presentation.viewmodel.CreateEditNoteViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.note.presentation.screen.CreateEditNoteScreen(
                noteId = null,
                isReadOnly = false,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            val viewModel =
                hiltViewModel<com.alejandro.habitjourney.features.note.presentation.viewmodel.CreateEditNoteViewModel>(
                    backStackEntry
                )
            com.alejandro.habitjourney.features.note.presentation.screen.CreateEditNoteScreen(
                noteId = noteId,
                isReadOnly = false,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // ==========================================
        // SETTINGS GRAPH
        // ==========================================

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToLanguage = { navController.navigate(Screen.LanguageSelection.route) },
                onNavigateToAuth = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Edit Profile Screen
        composable(Screen.EditProfile.route) { backStackEntry ->
            val editProfileViewModel = hiltViewModel<EditProfileViewModel>(backStackEntry)

            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onForceSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                viewModel = editProfileViewModel
            )
        }


        // Change Password Screen
        composable(Screen.ChangePassword.route) { backStackEntry ->
            val changePasswordViewModel = hiltViewModel<ChangePasswordViewModel>(backStackEntry)

            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


        // Language Selection Screen
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}