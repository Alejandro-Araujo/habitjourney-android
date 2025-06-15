package com.alejandro.habitjourney.features.task.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Representa una tarea en la base de datos local.
 *
 * Esta entidad está vinculada a un [UserEntity] y contiene toda la información
 * necesaria para una tarea, incluyendo fechas, prioridad y estado. Los índices
 * están optimizados para las consultas más comunes de filtrado y ordenación.
 *
 * @property id El identificador único autogenerado para la tarea.
 * @property userId El ID del [UserEntity] al que pertenece esta tarea.
 * @property title El título o nombre de la tarea.
 * @property description Una descripción opcional con más detalles sobre la tarea.
 * @property dueDate La fecha de vencimiento opcional para la tarea.
 * @property priority La prioridad de la tarea (BAJA, MEDIA, ALTA). Puede ser nula.
 * @property isCompleted `true` si la tarea ha sido marcada como completada.
 * @property completionDate La fecha en que se completó la tarea. Es nulo si no está completada.
 * @property isArchived `true` si la tarea está archivada.
 * @property createdAt Timestamp de la creación de la tarea.
 * @property reminderDateTime La fecha y hora exactas para un recordatorio. Es nulo si no hay recordatorio.
 * @property isReminderSet `true` si se ha establecido un recordatorio para esta tarea.
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borran sus tareas.
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["due_date"]),
        Index(value = ["is_completed"]),
        Index(value = ["is_archived"]),
        Index(value = ["priority"]),
        Index(value = ["created_at"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: LocalDate? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completion_date")
    val completionDate: LocalDate? = null,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "reminder_date_time")
    val reminderDateTime: LocalDateTime? = null,

    @ColumnInfo(name = "is_reminder_set")
    val isReminderSet: Boolean = false
)
