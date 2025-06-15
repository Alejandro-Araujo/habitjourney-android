package com.alejandro.habitjourney.features.task.domain.usecase

import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * **Caso de uso para obtener todas las tareas vencidas de un usuario.**
 *
 * Este caso de uso se encarga de identificar y recuperar las tareas que han sobrepasado su fecha de vencimiento
 * y que aún no han sido completadas ni archivadas. Delega la lógica de acceso a datos al [TaskRepository]
 * y utiliza la fecha actual del sistema para determinar qué tareas están vencidas.
 * Los resultados se emiten como un [Flow] para permitir la observación reactiva.
 *
 * @property taskRepository El repositorio de tareas que proporciona acceso a los datos de las tareas.
 */
class GetOverdueTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Recupera una lista de tareas vencidas para un usuario específico.
     * La determinación de si una tarea está vencida se basa en la fecha actual del sistema.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `GetOverdueTasksUseCase`
     * directamente como si fuera una función (por ejemplo, `getOverdueTasksUseCase(userId)`).
     *
     * @param userId El **ID** del usuario cuyas tareas vencidas se desean obtener.
     * @return Un [Flow] que emite una [List] de objetos [Task] que están vencidas para el usuario dado.
     */
    operator fun invoke(userId: Long): Flow<List<Task>> {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return taskRepository.getOverdueTasks(userId, currentDate)
    }
}