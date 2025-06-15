package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Caso de uso para archivar o desarchivar una tarea.
 * Este caso de uso encapsula la lógica de negocio para cambiar el estado de archivo de una tarea,
 * delegando la operación de persistencia al [TaskRepository].
 *
 * @property taskRepository El repositorio de tareas a través del cual se interactúa con los datos.
 */
class ArchiveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Hace que una tarea sea archivada o desarchivada.
     * Este es un operador de invocación, lo que permite llamar a la instancia de la clase
     * directamente como si fuera una función (e.g., `archiveTaskUseCase(taskId, isArchived)`).
     *
     * @param taskId El ID de la tarea que se va a archivar o desarchivar.
     * @param isArchived `true` para archivar la tarea, `false` para desarchivarla.
     * Por defecto, es `true` para archivar.
     */
    suspend operator fun invoke(taskId: Long, isArchived: Boolean = true) {
        taskRepository.archiveTask(taskId, isArchived)
    }
}