package com.alejandro.habitjourney.features.task.data.repository

import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

/**
 * Implementación de [ReminderRepository] que actúa como intermediario entre
 * la capa de dominio y el gestor de recordatorios local ([ReminderManager]).
 *
 * Delega las operaciones de programación, cancelación y actualización de recordatorios
 * al [ReminderManager], manteniendo la abstracción del repositorio en la capa de dominio.
 *
 * @property reminderManager El gestor de recordatorios que interactúa con el sistema de alarmas.
 */
class ReminderRepositoryImpl @Inject constructor(
    private val reminderManager: ReminderManager
) : ReminderRepository {

    /**
     * Programa un recordatorio para una tarea específica.
     * Delega la llamada a [ReminderManager.scheduleReminder].
     *
     * @param taskId El ID de la tarea a la que se asocia el recordatorio.
     * @param dateTime La fecha y hora en que debe activarse el recordatorio.
     * @param title El título del recordatorio.
     */
    override suspend fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String) {
        reminderManager.scheduleReminder(taskId, dateTime, title)
    }

    /**
     * Cancela un recordatorio existente para una tarea.
     * Delega la llamada a [ReminderManager.cancelReminder].
     *
     * @param taskId El ID de la tarea cuyo recordatorio se desea cancelar.
     */
    override suspend fun cancelReminder(taskId: Long) {
        reminderManager.cancelReminder(taskId)
    }

    /**
     * Actualiza un recordatorio existente para una tarea.
     * Delega la llamada a [ReminderManager.updateReminder].
     *
     * @param taskId El ID de la tarea cuyo recordatorio se desea actualizar.
     * @param newDateTime La nueva fecha y hora para el recordatorio.
     * @param title El nuevo título para el recordatorio.
     */
    override suspend fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String) {
        reminderManager.updateReminder(taskId, newDateTime, title)
    }

    /**
     * Verifica si un recordatorio está programado para una tarea específica.
     * Delega la llamada a [ReminderManager.isReminderScheduled].
     *
     * @param taskId El ID de la tarea a verificar.
     * @return `true` si el recordatorio está programado, `false` en caso contrario.
     */
    override suspend fun isReminderScheduled(taskId: Long): Boolean {
        return reminderManager.isReminderScheduled(taskId)
    }
}