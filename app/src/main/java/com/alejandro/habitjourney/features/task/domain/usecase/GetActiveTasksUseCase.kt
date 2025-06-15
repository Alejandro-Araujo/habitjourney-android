package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * **Caso de uso para obtener todas las tareas activas de un usuario.**
 *
 * Este caso de uso recupera las tareas que no han sido completadas ni archivadas.
 * Delega la recuperación de datos al [TaskRepository] y expone el resultado como un [Flow]
 * para permitir la observación de cambios en tiempo real.
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia de los datos.
 */
class GetActiveTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una lista de tareas activas para un usuario específico.
     *
     * Este es un operador de invocación (`operator fun invoke`), lo que te permite llamar a la instancia
     * de `GetActiveTasksUseCase` directamente como si fuera una función (por ejemplo, `getActiveTasksUseCase(userId)`).
     *
     * @param userId El **ID** del usuario cuyas tareas activas se desean obtener.
     * @return Un [Flow] que emite una [List] de objetos [Task] que están activas.
     */
    operator fun invoke(userId: Long): Flow<List<Task>> {
        return taskRepository.getActiveTasks(userId)
    }
}