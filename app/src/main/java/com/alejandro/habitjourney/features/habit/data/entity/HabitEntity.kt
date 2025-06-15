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

 /**
  * Entidad principal de hábitos con índices optimizados para consultas frecuentes.
  * Incluye foreign key cascada para eliminar logs al eliminar usuario.
  */
@Entity(
    tableName = "habits",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("user_id"),
        Index("frequency"),
        Index("is_archived"),
        Index("created_at")
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

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)