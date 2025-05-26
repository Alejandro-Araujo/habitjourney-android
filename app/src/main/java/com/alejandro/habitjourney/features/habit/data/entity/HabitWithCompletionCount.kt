package com.alejandro.habitjourney.features.habit.data.entity

import androidx.room.Embedded
import androidx.room.ColumnInfo

data class HabitWithCompletionCount(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "currentCompletionCount")
    val currentCompletionCount: Int
)