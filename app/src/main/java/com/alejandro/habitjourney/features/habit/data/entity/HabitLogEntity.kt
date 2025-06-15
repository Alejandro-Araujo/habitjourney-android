package com.alejandro.habitjourney.features.habit.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import kotlinx.datetime.LocalDate

/**
 * Entidad de logs con índice compuesto (habit_id, date) para consultas rápidas.
 * Foreign key cascada para mantener integridad referencial.
 */
@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habit_id"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index("habit_id", "date")]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "habit_id")
    val habitId: Long,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "status")
    val status: LogStatus,

    @ColumnInfo(name = "value")
    val value: Float? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)