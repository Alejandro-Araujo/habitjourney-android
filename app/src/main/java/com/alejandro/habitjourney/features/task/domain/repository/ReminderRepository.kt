package com.alejandro.habitjourney.features.task.domain.repository

import kotlinx.datetime.LocalDateTime

/**
 * Interfaz de repositorio para la gestión de recordatorios.
 *
 * Define las operaciones abstractas que la capa de dominio necesita para interactuar
 * con un sistema de recordatorios, desacoplándose de los detalles de implementación
 * de la capa de datos.
 */
interface ReminderRepository {
    /**
     * Programa un nuevo recordatorio para una tarea específica.
     *
     * @param taskId El ID de la tarea a la que se asocia el recordatorio.
     * @param dateTime La fecha y hora exactas en que el recordatorio debe activarse.
     * @param title El título del recordatorio, que se mostrará al usuario.
     */
    suspend fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String)
    /**
     * Cancela un recordatorio previamente programado para una tarea.
     *
     * @param taskId El ID de la tarea cuyo recordatorio se desea cancelar.
     */
    suspend fun cancelReminder(taskId: Long)
    /**
     * Actualiza un recordatorio existente para una tarea con una nueva fecha y hora.
     *
     * @param taskId El ID de la tarea cuyo recordatorio se desea actualizar.
     * @param newDateTime La nueva fecha y hora para el recordatorio.
     * @param title El nuevo título para el recordatorio (puede ser el mismo si no ha cambiado).
     */
    suspend fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String)
    /**
     * Verifica si un recordatorio está actualmente programado para una tarea específica.
     *
     * @param taskId El ID de la tarea a verificar.
     * @return `true` si un recordatorio está programado para la tarea, `false` en caso contrario.
     */
    suspend fun isReminderScheduled(taskId: Long): Boolean
}