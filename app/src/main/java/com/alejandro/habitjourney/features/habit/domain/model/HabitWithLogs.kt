package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.LogStatus


data class HabitWithLogs(
    val habit: Habit,
    val logs: List<HabitLog>
) {
    val currentStreak: Int
        get() = calculateCurrentStreak()

    val bestStreak: Int
        get() = calculateBestStreak()

    val completionRate: Float
        get() = calculateCompletionRate()

    private fun calculateCurrentStreak(): Int {
        if (logs.isEmpty()) return 0

        val sortedLogs = logs.sortedByDescending { it.date }
        var streak = 0

        for (log in sortedLogs) {
            if (log.status == LogStatus.COMPLETED) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateBestStreak(): Int {
        if (logs.isEmpty()) return 0

        val sortedLogs = logs.sortedBy { it.date }
        var maxStreak = 0
        var currentStreak = 0

        for (log in sortedLogs) {
            if (log.status == LogStatus.COMPLETED) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 0
            }
        }

        return maxStreak
    }

    private fun calculateCompletionRate(): Float {
        if (logs.isEmpty()) return 0f

        val completedCount = logs.count { it.status == LogStatus.COMPLETED }
        return (completedCount.toFloat() / logs.size) * 100f
    }
}