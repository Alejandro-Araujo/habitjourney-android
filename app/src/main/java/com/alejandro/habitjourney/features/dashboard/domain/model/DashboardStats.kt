package com.alejandro.habitjourney.features.dashboard.domain.model

data class DashboardStats(
    // Hábitos
    val totalHabitsToday: Int,
    val completedHabitsToday: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyHabitCompletionRate: Float,

    // Tareas
    val totalActiveTasks: Int,
    val completedTasksToday: Int,
    val overdueTasks: Int,

    // Notas
    val totalActiveNotes: Int,
    val totalWords: Int,
    val notesCreatedThisWeek: Int,

    // General
    val productiveDaysThisMonth: Int
)