package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

/**
 * **Caso de uso para actualizar un recordatorio existente de una tarea.**
 *
 * Este caso de uso maneja la lógica para modificar un recordatorio previamente programado para una tarea.
 * Permite cambiar la fecha, hora y el título del recordatorio, delegando la operación
 * de actualización al [ReminderRepository].
 *
 * @property reminderRepository El repositorio encargado de la gestión de recordatorios.
 */
class UpdateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    /**
     * Actualiza un recordatorio asociado a una tarea con una nueva fecha, hora y título.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `UpdateReminderUseCase`
     * directamente como si fuera una función (por ejemplo, `updateReminderUseCase(taskId, newDateTime, title)`).
     *
     * @param taskId El **ID** de la tarea cuyo recordatorio se desea actualizar.
     * @param newDateTime La nueva [LocalDateTime] para el recordatorio.
     * @param title El nuevo título para el recordatorio, que puede ser el título de la tarea actualizado.
     */
    suspend operator fun invoke(taskId: Long, newDateTime: LocalDateTime, title: String) {
        reminderRepository.updateReminder(taskId, newDateTime, title)
    }
}