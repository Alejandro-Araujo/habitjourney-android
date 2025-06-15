package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * **Caso de uso para la creación de nuevas tareas.**
 *
 * Este caso de uso encapsula la lógica para añadir una nueva tarea al sistema.
 * Delega la operación de persistencia al [TaskRepository].
 *
 * @property taskRepository El repositorio de tareas que maneja la persistencia de los datos.
 */
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Crea una nueva tarea en la base de datos.
     *
     * Este es un operador de invocación (`operator fun invoke`), lo que te permite llamar a la instancia
     * de `CreateTaskUseCase` directamente como si fuera una función (por ejemplo, `createTaskUseCase(newTask)`).
     *
     * @param task El objeto [Task] que representa la tarea a crear.
     * @return El **ID** de la tarea recién creada en la base de datos.
     */
    suspend operator fun invoke(task: Task): Long {
        return taskRepository.insertTask(task)
    }
}