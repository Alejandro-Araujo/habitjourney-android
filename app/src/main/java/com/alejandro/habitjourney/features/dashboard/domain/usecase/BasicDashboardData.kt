package com.alejandro.habitjourney.features.dashboard.domain.usecase

data class BasicDashboardData(
    val user: com.alejandro.habitjourney.features.user.domain.model.User?,
    val todayHabitsWithCount: List<Pair<com.alejandro.habitjourney.features.habit.domain.model.Habit, Int>>,
    val activeTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>,
    val completedTasksToday: List<com.alejandro.habitjourney.features.task.domain.model.Task>,
    val overdueTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>
)

data class NotesAndStats(
    val activeNotes: List<com.alejandro.habitjourney.features.note.domain.model.Note>,
    val streaks: Pair<Int, Int>,
    val productiveDays: Int
)
