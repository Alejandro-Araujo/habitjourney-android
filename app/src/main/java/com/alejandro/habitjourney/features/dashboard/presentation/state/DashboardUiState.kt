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
    val isEmpty: Boolean = totalHabitsToday == 0 && totalActiveTasks == 0 && totalNotes == 0
) {
    // Computed properties for UI
    val habitCompletionPercentage: Float
        get() = if (totalHabitsToday > 0) {
            completedHabitsToday.toFloat() / totalHabitsToday
        } else 0f

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

    val summaryMessage: String
        get() = when {
            isEmpty -> "¡Comienza tu viaje creando tus primeros hábitos y tareas!"
            completedHabitsToday == totalHabitsToday && !hasOverdueTasks ->
                "¡Excelente día! Has completado todos tus hábitos${if (currentStreak > 1) " y mantienes una racha de $currentStreak días" else ""}."
            habitCompletionPercentage >= 0.8f && !hasOverdueTasks ->
                "¡Muy buen progreso! Has completado el ${(habitCompletionPercentage * 100).toInt()}% de tus hábitos."
            hasOverdueTasks ->
                "Tienes $overdueTasks tarea${if (overdueTasks > 1) "s" else ""} vencida${if (overdueTasks > 1) "s" else ""}. ¡Es hora de ponerse al día!"
            habitCompletionPercentage >= 0.5f ->
                "Buen avance del día. ¡Sigue así para completar todos tus hábitos!"
            else ->
                "¡Aún queda mucho día por delante! Es momento de trabajar en tus hábitos."
        }

    val motivationalQuote: String
        get() = when (productivityScore) {
            in 90..100 -> "¡Eres imparable! Tu constancia está dando frutos."
            in 70..89 -> "¡Excelente trabajo! Cada día es un paso hacia tus metas."
            in 50..69 -> "¡Sigue adelante! El progreso es progreso, sin importar lo pequeño."
            else -> "Cada nuevo día es una oportunidad para mejorar. ¡Tú puedes!"
        }
}