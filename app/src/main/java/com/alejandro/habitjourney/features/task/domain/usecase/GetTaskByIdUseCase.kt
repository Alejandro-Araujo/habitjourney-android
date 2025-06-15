package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * **Caso de uso para obtener una tarea específica por su identificador único.**
 *
 * Este caso de uso proporciona la funcionalidad para recuperar los detalles de una única tarea
 * basándose en su ID. Delega la operación de consulta al [TaskRepository] y emite el resultado
 * como un [Flow] para permitir la observación reactiva.
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia y acceso a los datos.
 */
class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una tarea específica utilizando su ID.
     *
     * Al utilizar el operador `invoke`, puedes llamar a la instancia de `GetTaskByIdUseCase`
     * directamente como si fuera una función (por ejemplo, `getTaskByIdUseCase(taskId)`).
     *
     * @param taskId El **ID** de la tarea que se desea obtener.
     * @return Un [Flow] que emite el objeto [Task] correspondiente al ID, o `null` si la tarea no se encuentra.
     */
    operator fun invoke(taskId: Long): Flow<Task?> {
        return taskRepository.getTaskById(taskId)
    }
}