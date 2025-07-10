package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * **Caso de uso para obtener todas las tareas completadas de un usuario.**
 *
 * Este caso de uso se encarga de recuperar las tareas que un usuario ha marcado como completadas.
 * Delega la solicitud de datos al [TaskRepository] y emite los resultados como un [Flow],
 * lo que permite que los observadores reaccionen a los cambios en la lista de tareas completadas.
 *
 * @property taskRepository El repositorio de tareas que proporciona acceso a los datos de las tareas.
 */
class GetCompletedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una lista de tareas completadas para un usuario específico.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `GetCompletedTasksUseCase`
     * como si fuera una función (por ejemplo, `getCompletedTasksUseCase(userId)`).
     *
     * @param userId El **ID** del usuario cuyas tareas completadas deseas obtener.
     * @return Un [Flow] que emite una [List] de objetos [Task] que están completadas para el usuario dado.
     */
    operator fun invoke(userId: String): Flow<List<Task>> {
        return taskRepository.getCompletedTasks(userId)
    }
}