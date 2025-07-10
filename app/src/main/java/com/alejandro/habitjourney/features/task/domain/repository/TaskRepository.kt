package com.alejandro.habitjourney.features.task.domain.repository

import com.alejandro.habitjourney.features.task.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Interfaz de repositorio para la gestión de tareas.
 * Define las operaciones que la capa de dominio necesita para interactuar con los datos de las tareas,
 * sin exponer los detalles de la implementación de la capa de datos.
 */
interface TaskRepository {
    /**
     * Inserta una nueva tarea en el sistema de persistencia.
     * @param task El objeto [Task] a insertar.
     * @return El ID de la tarea recién insertada.
     */
    suspend fun insertTask(task: Task): Long

    /**
     * Actualiza una tarea existente en el sistema de persistencia.
     * @param task El objeto [Task] con los datos actualizados.
     */
    suspend fun updateTask(task: Task)

    /**
     * Elimina una tarea del sistema de persistencia por su ID.
     * @param taskId El ID de la tarea a eliminar.
     */
    suspend fun deleteTask(taskId: Long)

    /**
     * Obtiene una tarea específica por su ID.
     * @param taskId El ID de la tarea a recuperar.
     * @return Un [Flow] que emite el objeto [Task] o `null` si no se encuentra.
     */
    fun getTaskById(taskId: Long): Flow<Task?>

    /**
     * Obtiene todas las tareas activas (no completadas y no archivadas) para un usuario específico.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [Task] activas.
     */
    fun getActiveTasks(userId: String): Flow<List<Task>>

    /**
     * Obtiene todas las tareas completadas para un usuario específico.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [Task] completadas.
     */
    fun getCompletedTasks(userId: String): Flow<List<Task>>

    /**
     * Obtiene todas las tareas archivadas para un usuario específico.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [Task] archivadas.
     */
    fun getArchivedTasks(userId: String): Flow<List<Task>>

    /**
     * Obtiene todas las tareas vencidas (con fecha de vencimiento anterior a la actual y no completadas/archivadas)
     * para un usuario específico.
     * @param userId El ID del usuario.
     * @param currentDate La fecha actual para comparar con las fechas de vencimiento.
     * @return Un [Flow] que emite una lista de [Task] vencidas.
     */
    fun getOverdueTasks(userId: String, currentDate: LocalDate): Flow<List<Task>>

    /**
     * Obtiene todas las tareas (no archivadas) para un usuario específico.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de todas las [Task] no archivadas.
     */
    fun getAllTasks(userId: String): Flow<List<Task>>

    /**
     * Cambia el estado de completado de una tarea y actualiza su fecha de finalización.
     * @param taskId El ID de la tarea a actualizar.
     * @param isCompleted `true` para marcar como completada, `false` para marcar como incompleta.
     * @param completionDate La fecha de finalización si se marca como completada, o `null` si se marca como incompleta.
     */
    suspend fun setCompleted(taskId: Long, isCompleted: Boolean, completionDate: LocalDate?)

    /**
     * Archiva o desarchiva una tarea según el parámetro [isArchived].
     * @param taskId El ID de la tarea a archivar o desarchivar.
     * @param isArchived `true` para archivar la tarea, `false` para desarchivarla.
     */
    suspend fun archiveTask(taskId: Long, isArchived: Boolean)

    /**
     * Obtiene las tareas completadas por un usuario en una fecha específica.
     * @param userId El ID del usuario.
     * @param date La fecha para la cual se quieren obtener las tareas completadas.
     * @return Un [Flow] que emite una lista de [Task] completadas en la fecha especificada.
     */
    fun getCompletedTasksToday(userId: String, date: LocalDate): Flow<List<Task>>

    /**
     * Obtiene todas las tareas relevantes para un día específico para un usuario.
     * Incluye tareas programadas, completadas y sin fecha específica.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se buscan las tareas relevantes.
     * @return Un [Flow] que emite una lista de [Task] relevantes para la fecha.
     */
    fun getTasksForDate(userId: String, date: LocalDate): Flow<List<Task>>

    /**
     * Cuenta el número total de tareas programadas o relevantes para una fecha específica para un usuario.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas.
     * @return El número total de tareas relevantes para la fecha especificada.
     */
    suspend fun getTaskCountForDate(userId: String, date: LocalDate): Int

    /**
     * Cuenta el número de tareas completadas por un usuario en una fecha específica.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas completadas.
     * @return El número de tareas completadas en la fecha especificada.
     */
    suspend fun getCompletedTaskCountForDate(userId: String, date: LocalDate): Int
}