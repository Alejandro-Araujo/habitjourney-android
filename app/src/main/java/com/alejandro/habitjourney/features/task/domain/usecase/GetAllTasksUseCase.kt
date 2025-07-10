package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * **Caso de uso para obtener todas las tareas (no archivadas) de un usuario.**
 *
 * Este caso de uso recupera todas las tareas asociadas a un usuario que no han sido archivadas.
 * Delega la obtención de los datos al [TaskRepository] y emite los resultados
 * como un [Flow] para permitir la observación reactiva.
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia y acceso a los datos de las tareas.
 */
class GetAllTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una lista de todas las tareas no archivadas para un usuario específico.
     *
     * Este es un operador de invocación (`operator fun invoke`), lo que te permite llamar a la instancia
     * de `GetAllTasksUseCase` directamente como si fuera una función (por ejemplo, `getAllTasksUseCase(userId)`).
     *
     * @param userId El **ID** del usuario cuyas tareas se desean obtener.
     * @return Un [Flow] que emite una [List] de objetos [Task] que no están archivadas para el usuario dado.
     */
    operator fun invoke(userId: String): Flow<List<Task>> {
        return taskRepository.getAllTasks(userId)
    }
}