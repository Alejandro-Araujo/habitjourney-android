package com.alejandro.habitjourney.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "progress",
    primaryKeys = ["user_id"],
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE)]
)
data class ProgressEntity(
    @PrimaryKey @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "total_habits_completed")
    val totalHabitsCompleted: Int = 0,

    @ColumnInfo(name = "total_xp")
    val totalXp: Int = 0,

    @ColumnInfo(name = "current_streak")
    val currentStreak: Int = 0,

    @ColumnInfo(name = "longest_streak")
    val longestStreak: Int = 0,
)