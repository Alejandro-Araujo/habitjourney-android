package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import javax.inject.Inject

/**
 * Caso de uso para cancelar un recordatorio existente para una tarea.
 * Este caso de uso abstrae la lógica para interactuar con el [ReminderRepository]
 * y asegurar que un recordatorio programado para una tarea específica sea cancelado.
 *
 * @property reminderRepository El repositorio encargado de la gestión de recordatorios.
 */
class CancelReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    /**
     * Cancela un recordatorio asociado a una tarea específica.
     * Este es un operador de invocación, lo que permite llamar a la instancia de la clase
     * directamente como si fuera una función (e.g., `cancelReminderUseCase(taskId)`).
     *
     * @param taskId El ID de la tarea cuyo recordatorio se desea cancelar.
     */
    suspend operator fun invoke(taskId: Long) {
        reminderRepository.cancelReminder(taskId)
    }
}