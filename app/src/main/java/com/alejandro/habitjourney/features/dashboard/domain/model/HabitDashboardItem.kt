package com.alejandro.habitjourney.features.dashboard.domain.model

import com.alejandro.habitjourney.features.habit.domain.model.Habit


data class HabitDashboardItem(
    val habit: Habit,
    val completionCount: Int,
    val isCompleted: Boolean,
    val isSkipped: Boolean,
    val completionPercentage: Float
)