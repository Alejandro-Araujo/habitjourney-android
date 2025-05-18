package com.alejandro.habitjourney.features.progress.data.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity

@Entity(
    tableName = "progress",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("user_id")]
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