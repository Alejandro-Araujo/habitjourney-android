package com.alejandro.habitjourney.features.task.domain.model

import com.alejandro.habitjourney.core.data.local.enums.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Representa el modelo de dominio de una tarea.
 * Este modelo encapsula los datos esenciales de una tarea que se utiliza en la capa de negocio
 * y la interfaz de usuario. No contiene lógica de persistencia ni detalles de implementación
 * específicos de la base de datos.
 *
 * @property id Identificador único de la tarea. Generado automáticamente para nuevas tareas (por defecto 0L).
 * @property userId Identificador del usuario al que pertenece esta tarea.
 * @property title Título de la tarea, un campo obligatorio y conciso.
 * @property description Descripción detallada de la tarea (opcional).
 * @property dueDate Fecha de vencimiento de la tarea (opcional).
 * @property priority Nivel de prioridad de la tarea (e.g., [Priority.HIGH], [Priority.MEDIUM], [Priority.LOW]) (opcional).
 * @property isCompleted Indica si la tarea ha sido completada (por defecto `false`).
 * @property completionDate Fecha en la que la tarea fue completada (opcional, solo presente si [isCompleted] es `true`).
 * @property isArchived Indica si la tarea ha sido archivada (por defecto `false`).
 * @property createdAt Marca de tiempo de la creación de la tarea en milisegundos.
 * @property reminderDateTime Fecha y hora específicas para un recordatorio de la tarea (opcional).
 * @property isReminderSet Indica si hay un recordatorio establecido para esta tarea (por defecto `false`).
 */
data class Task(
    val id: Long = 0L,
    val userId: String,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val priority: Priority? = null,
    val isCompleted: Boolean = false,
    val completionDate: LocalDate? = null,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val reminderDateTime: LocalDateTime? = null,
    val isReminderSet: Boolean = false
)