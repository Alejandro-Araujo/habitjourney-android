package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * **Caso de uso para eliminar una tarea existente.**
 *
 * Este caso de uso encapsula la lógica para remover una tarea específica del sistema.
 * Delega la operación de eliminación al [TaskRepository].
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia de los datos.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Elimina una tarea de la base de datos por su identificador único.
     *
     * Este es un operador de invocación (`operator fun invoke`), lo que te permite llamar a la instancia
     * de `DeleteTaskUseCase` directamente como si fuera una función (por ejemplo, `deleteTaskUseCase(taskId)`).
     *
     * @param taskId El **ID** de la tarea que se va a eliminar.
     */
    suspend operator fun invoke(taskId: Long) {
        taskRepository.deleteTask(taskId)
    }
}