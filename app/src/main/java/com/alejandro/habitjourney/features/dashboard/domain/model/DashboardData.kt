package com.alejandro.habitjourney.features.dashboard.domain.model

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.user.domain.model.User

data class DashboardData(
    val user: User?,
    val todayHabits: List<HabitWithLogs>,
    val pendingTasks: List<Task>,
    val recentNotes: List<Note>,
    val dashboardStats: DashboardStats
)