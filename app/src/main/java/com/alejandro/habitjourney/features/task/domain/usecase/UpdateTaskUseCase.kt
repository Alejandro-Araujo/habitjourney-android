package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * **Caso de uso para actualizar una tarea existente.**
 *
 * Este caso de uso encapsula la lógica para modificar los detalles de una tarea ya existente.
 * Delega la operación de actualización al [TaskRepository], asegurando que los cambios
 * se persistan correctamente.
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia de los datos.
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Actualiza una tarea existente en la base de datos.
     *
     * Este es un operador de invocación (`operator fun invoke`), lo que te permite llamar a la instancia
     * de `UpdateTaskUseCase` directamente como si fuera una función (por ejemplo, `updateTaskUseCase(taskToUpdate)`).
     *
     * @param task El objeto [Task] con los datos actualizados de la tarea. El [Task.id] debe corresponder
     * a una tarea existente.
     */
    suspend operator fun invoke(task: Task) {
        taskRepository.updateTask(task)
    }
}