package com.alejandro.habitjourney.navigation

/**
 * Sealed class que define todas las rutas de navegación de la aplicación.
 * Cada screen tiene su ruta única y parámetros opcionales.
 */
sealed class Screen(val route: String) {

    // Authentication Routes
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Main App Routes
    data object Dashboard : Screen("dashboard")

    // Habit Routes
    data object HabitList : Screen("habit_list")
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    data object CreateEditHabit : Screen("create_edit_habit?habitId={habitId}") {
        fun createRoute(habitId: Long? = null) = if (habitId != null) {
            "create_edit_habit?habitId=$habitId"
        } else {
            "create_edit_habit"
        }
    }

    // Task Routes - NUEVAS RUTAS AÑADIDAS
    data object TaskList : Screen("task_list")
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }
    data object CreateTask : Screen("create_task")
    data object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Long) = "edit_task/$taskId"
    }

    // Note Routes
    data object NoteList : Screen("note_list")
    data object CreateNote : Screen("create_note")
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: Long) = "edit_note/$noteId"
    }

    // Achievement Routes
    data object Achievements : Screen("achievements")

    // Settings Routes
    data object Settings : Screen("settings")

    // Progress Routes
    data object Progress : Screen("progress")
}

/**
 * Rutas de navegación agrupadas por funcionalidad
 */
object NavigationRoutes {

    // Rutas que requieren autenticación y son parte del grafo principal
    val authenticatedRoutes = listOf(
        Screen.Dashboard.route,
        Screen.HabitList.route,
        Screen.HabitDetail.route,
        Screen.CreateEditHabit.route,
        Screen.TaskList.route,
        Screen.TaskDetail.route,
        Screen.CreateTask.route,
        Screen.EditTask.route,
        Screen.NoteList.route,
        Screen.CreateNote.route,
        Screen.EditNote.route,
        Screen.Achievements.route,
        Screen.Settings.route,
        Screen.Progress.route
    )

    // Rutas de autenticación
    val authRoutes = listOf(
        Screen.Login.route,
        Screen.Register.route
    )

    // Rutas principales del bottom navigation
    val bottomNavRoutes = listOf(
        Screen.Dashboard.route,
        Screen.HabitList.route,
        Screen.TaskList.route,
        Screen.NoteList.route,
        Screen.Progress.route
    )
}