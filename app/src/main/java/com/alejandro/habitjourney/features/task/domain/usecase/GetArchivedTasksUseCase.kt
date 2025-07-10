package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * **Caso de uso para obtener todas las tareas archivadas de un usuario.**
 *
 * Este caso de uso se encarga de recuperar las tareas que un usuario ha marcado como archivadas.
 * Delega la solicitud de datos al [TaskRepository] y proporciona los resultados como un [Flow],
 * lo que permite que los observadores reaccionen a los cambios en la lista de tareas archivadas.
 *
 * @property taskRepository El repositorio de tareas que ofrece acceso a los datos de las tareas.
 */
class GetArchivedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una lista de tareas archivadas para un usuario específico.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `GetArchivedTasksUseCase`
     * como si fuera una función (por ejemplo, `getArchivedTasksUseCase(userId)`).
     *
     * @param userId El **ID** del usuario cuyas tareas archivadas deseas obtener.
     * @return Un [Flow] que emite una [List] de objetos [Task] que están archivadas para el usuario dado.
     */
    operator fun invoke(userId: String): Flow<List<Task>> {
        return taskRepository.getArchivedTasks(userId)
    }
}