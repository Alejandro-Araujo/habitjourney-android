package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * **Caso de uso para alternar el estado de completado de una tarea.**
 *
 * Este caso de uso maneja la lógica para marcar una tarea como completada o incompleta.
 * Cuando una tarea se marca como completada, registra la fecha actual de finalización.
 * Delega la actualización de la tarea al [TaskRepository].
 *
 * @property taskRepository El repositorio de tareas que gestiona las operaciones de persistencia.
 */
class ToggleTaskCompletionUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Alterna el estado de completado de una tarea.
     * Si `isCompleted` es `true`, la tarea se marcará como completada y se establecerá
     * la fecha de finalización a la fecha actual del sistema. Si `isCompleted` es `false`,
     * la tarea se marcará como incompleta y la fecha de finalización se establecerá en `null`.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `ToggleTaskCompletionUseCase`
     * directamente como si fuera una función (por ejemplo, `toggleTaskCompletionUseCase(taskId, true)`).
     *
     * @param taskId El **ID** de la tarea cuyo estado de completado se desea alternar.
     * @param isCompleted El nuevo estado de completado: `true` para completada, `false` para incompleta.
     */
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) {
        val completionDate = if (isCompleted) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        } else null

        taskRepository.setCompleted(taskId, isCompleted, completionDate)
    }
}