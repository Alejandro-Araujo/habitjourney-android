package com.alejandro.habitjourney.features.habit.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relación de Room para obtener hábito con todos sus logs.
 * Útil para análisis detallados y gráficos de progreso.
 */
data class HabitWithLogs(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
        )
    val logs: List<HabitLogEntity>
)
