package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

/**
 * **Caso de uso para programar un recordatorio para una tarea.**
 *
 * Este caso de uso encapsula la lógica para establecer un recordatorio para una tarea específica.
 * Delega la programación real del recordatorio al [ReminderRepository],
 * asegurando una clara separación de responsabilidades.
 *
 * @property reminderRepository El repositorio encargado de la gestión de recordatorios.
 */
class ScheduleReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    /**
     * Programa un recordatorio para una tarea en una fecha y hora determinadas.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `ScheduleReminderUseCase`
     * directamente como si fuera una función (por ejemplo, `scheduleReminderUseCase(taskId, dateTime, title)`).
     *
     * @param taskId El **ID** de la tarea para la que se programará el recordatorio.
     * @param dateTime La [LocalDateTime] exacta en la que se activará el recordatorio.
     * @param title El título del recordatorio, que generalmente es el título de la tarea.
     */
    suspend operator fun invoke(taskId: Long, dateTime: LocalDateTime, title: String) {
        reminderRepository.scheduleReminder(taskId, dateTime, title)
    }
}