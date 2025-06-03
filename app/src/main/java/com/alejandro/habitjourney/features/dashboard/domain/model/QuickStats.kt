package com.alejandro.habitjourney.features.dashboard.domain.model


data class QuickStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val completedToday: Int,
    val totalToday: Int,
    val weeklyCompletionRate: Float,
    val productiveDaysThisMonth: Int
)
