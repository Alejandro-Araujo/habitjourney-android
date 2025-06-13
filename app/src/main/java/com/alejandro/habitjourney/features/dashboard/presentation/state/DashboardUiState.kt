package com.alejandro.habitjourney.features.dashboard.presentation.state

import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.user.domain.model.User

data class DashboardUiState(
    // Loading & Error states
    val isLoading: Boolean = true,
    val error: String? = null,

    // User data
    val user: User? = null,

    // Habits data
    val todayHabits: List<HabitWithLogs> = emptyList(),
    val totalHabitsToday: Int = 0,
    val completedHabitsToday: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyCompletionRate: Float = 0f,

    // Tasks data
    val activeTasks: List<Task> = emptyList(),
    val totalActiveTasks: Int = 0,
    val overdueTasks: Int = 0,

    // Notes data
    val recentNotes: List<Note> = emptyList(),
    val totalNotes: Int = 0,
    val totalWords: Int = 0,

    // General stats
    val productiveDaysThisMonth: Int = 0,

    // Additional computed properties
    val isEmpty: Boolean = false, // Lo calcula el ViewModel

    // --- SECCIÓN DE MENSAJES ---
    // Ahora son simples strings que recibe del ViewModel
    val summaryMessage: String = "",
    val motivationalQuote: String = ""
) {
    // Estas propiedades se quedan porque son cálculos puros sobre los datos del propio state
    val habitCompletionPercentage: Float
        get() = if (totalHabitsToday > 0) completedHabitsToday.toFloat() / totalHabitsToday else 0f

    val hasOverdueTasks: Boolean
        get() = overdueTasks > 0

    val productivityScore: Int
        get() {
            val habitScore = if (totalHabitsToday > 0) {
                (completedHabitsToday.toFloat() / totalHabitsToday * 100).toInt()
            } else 100

            val taskScore = when {
                totalActiveTasks == 0 -> 100
                hasOverdueTasks -> 70
                else -> 90
            }

            val streakBonus = when {
                currentStreak >= 30 -> 10
                currentStreak >= 7 -> 5
                currentStreak >= 3 -> 2
                else -> 0
            }

            return ((habitScore * 0.6 + taskScore * 0.4).toInt() + streakBonus).coerceIn(0, 100)
        }
}