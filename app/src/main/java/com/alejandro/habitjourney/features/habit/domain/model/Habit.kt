// com.alejandro.habitjourney.features.habit.domain.model.Habit.kt
package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate

data class Habit(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val type: HabitType,
    val frequency: String,
    val frequencyDays: List<Weekday>?,
    val dailyTarget: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isArchived: Boolean,
    val createdAt: Long
)