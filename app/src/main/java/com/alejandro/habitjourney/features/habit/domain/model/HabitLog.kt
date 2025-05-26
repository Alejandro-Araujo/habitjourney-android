package com.alejandro.habitjourney.features.habit.domain.model


import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import kotlinx.datetime.LocalDate

data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val status: LogStatus,
    val value: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
