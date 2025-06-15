package com.alejandro.habitjourney.features.habit.data.entity

import androidx.room.Embedded
import androidx.room.ColumnInfo

/**
 * DTO para consultas que incluyen conteo de completaciones.
 * Usado en dashboard para mostrar progreso actual.
 */
data class HabitWithCompletionCount(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "currentCompletionCount")
    val currentCompletionCount: Int
)