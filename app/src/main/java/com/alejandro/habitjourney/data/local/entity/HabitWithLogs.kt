package com.alejandro.habitjourney.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithLogs(
    @Embedded val habit: HabitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "habit_id"
        )
    val logs: List<HabitLogEntity>
)
