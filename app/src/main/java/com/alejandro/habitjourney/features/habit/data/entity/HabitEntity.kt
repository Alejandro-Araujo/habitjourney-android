 package com.alejandro.habitjourney.features.habit.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "habits",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.NO_ACTION
    )],
    indices = [
        Index("user_id"),
        // Index("is_active"), // Ya puedes eliminar este índice si isActive no existe.
        Index("frequency"),
        Index("is_archived") // ¡NUEVO ÍNDICE PARA `is_archived`!
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "type")
    val type: HabitType,

    @ColumnInfo(name = "frequency")
    val frequency: String,

    @ColumnInfo(name = "frequency_days")
    val frequencyDays: List<Weekday>? = null,

    @ColumnInfo(name = "daily_target")
    val dailyTarget: Int? = null,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate? = null,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,

    // @ColumnInfo(name = "is_active") // ¡ELIMINA ESTA LÍNEA!
    // val isActive: Boolean = true, // ¡ELIMINA ESTA LÍNEA!

    @ColumnInfo(name = "is_archived") // ¡CAMBIADO DE `is_deleted` A `is_archived`!
    val isArchived: Boolean = false, // ¡CAMBIADO DE `isDeleted` A `isArchived`!

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)