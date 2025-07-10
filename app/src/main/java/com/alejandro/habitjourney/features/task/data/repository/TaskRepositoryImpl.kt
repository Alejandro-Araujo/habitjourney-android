package com.alejandro.habitjourney.features.task.data.repository

import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.task.data.mapper.TaskMapper.toDomain
import com.alejandro.habitjourney.features.task.data.mapper.TaskMapper.toEntity
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Implementación de [TaskRepository] que actúa como un intermediario entre la capa de datos
 * (a través de [TaskDao]) y la capa de dominio.
 * Se encarga de mapear las entidades de datos a modelos de dominio y viceversa,
 * y de orquestar las operaciones de la base de datos.
 *
 * @property taskDao El Data Access Object para interactuar con la base de datos de tareas.
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    /**
     * Inserta una nueva tarea en la base de datos.
     * Convierte el modelo de dominio [Task] a una entidad [TaskEntity] antes de la inserción.
     * @param task El modelo de dominio de la tarea a insertar.
     * @return El ID de la tarea insertada.
     */
    override suspend fun insertTask(task: Task): Long {
        val taskId = taskDao.insert(task.toEntity())
        return taskId
    }

    /**
     * Actualiza una tarea existente en la base de datos.
     * Convierte el modelo de dominio [Task] a una entidad [TaskEntity] antes de la actualización.
     * @param task El modelo de dominio de la tarea a actualizar.
     */
    override suspend fun updateTask(task: Task) {
        taskDao.update(task.toEntity())
    }

    /**
     * Elimina una tarea de la base de datos por su ID.
     * @param taskId El ID de la tarea a eliminar.
     */
    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    /**
     * Obtiene una tarea por su ID como un [Flow] de [Task].
     * Mapea la entidad [TaskEntity] a un modelo de dominio [Task].
     * @param taskId El ID de la tarea a recuperar.
     * @return Un [Flow] que emite el modelo de dominio [Task] o `null` si no se encuentra.
     */
    override fun getTaskById(taskId: Long): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomain() }
    }

    /**
     * Obtiene todas las tareas activas (no completadas y no archivadas) para un usuario.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] activos.
     */
    override fun getActiveTasks(userId: String): Flow<List<Task>> {
        return taskDao.getActiveTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    /**
     * Obtiene todas las tareas completadas para un usuario.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] completados.
     */
    override fun getCompletedTasks(userId: String): Flow<List<Task>> {
        return taskDao.getCompletedTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    /**
     * Obtiene todas las tareas archivadas para un usuario.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] archivados.
     */
    override fun getArchivedTasks(userId: String): Flow<List<Task>> {
        return taskDao.getArchivedTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    /**
     * Obtiene todas las tareas vencidas para un usuario y la fecha actual.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @param currentDate La fecha actual para la comparación.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] vencidos.
     */
    override fun getOverdueTasks(userId: String, currentDate: LocalDate): Flow<List<Task>> {
        return taskDao.getOverdueTasks(userId, currentDate).map { entities ->
            entities.toDomain()
        }
    }

    /**
     * Obtiene todas las tareas no archivadas para un usuario.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de todos los modelos de dominio [Task] no archivados.
     */
    override fun getAllTasks(userId: String): Flow<List<Task>> {
        return taskDao.getAllTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    /**
     * Marca una tarea como completada o incompleta y actualiza su fecha de finalización.
     * @param taskId El ID de la tarea a actualizar.
     * @param isCompleted `true` para marcar como completada, `false` para marcar como incompleta.
     * @param completionDate La fecha de finalización si se marca como completada, o `null` si se marca como incompleta.
     */
    override suspend fun setCompleted(
        taskId: Long,
        isCompleted: Boolean,
        completionDate: LocalDate?
    ) {
        taskDao.setCompleted(taskId, isCompleted, completionDate)
    }

    /**
     * Archiva o desarchiva una tarea según el valor de `isArchived`.
     * @param taskId El ID de la tarea a archivar/desarchivar.
     * @param isArchived `true` para archivar, `false` para desarchivar.
     */
    override suspend fun archiveTask(taskId: Long, isArchived: Boolean) {
        if (isArchived) {
            taskDao.archiveTask(taskId)
        } else {
            taskDao.unarchiveTask(taskId)
        }
    }

    /**
     * Obtiene las tareas completadas para un usuario en una fecha específica.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @param date La fecha para la que se buscan las tareas completadas.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] completadas en la fecha dada.
     */
    override fun getCompletedTasksToday(userId: String, date: LocalDate): Flow<List<Task>> {
        return taskDao.getCompletedTasksByDate(userId, date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene las tareas relevantes para una fecha específica para un usuario.
     * Incluye tareas con fecha de vencimiento en la fecha dada, completadas en la fecha dada,
     * o tareas sin fecha de vencimiento creadas antes o en la fecha dada.
     * Mapea la lista de entidades [TaskEntity] a una lista de modelos de dominio [Task].
     * @param userId El ID del usuario.
     * @param date La fecha para la que se buscan las tareas.
     * @return Un [Flow] que emite una lista de modelos de dominio [Task] relevantes para la fecha.
     */
    override fun getTasksForDate(userId: String, date: LocalDate): Flow<List<Task>> {
        return taskDao.getTasksForDate(userId, date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Cuenta el número de tareas programadas o relevantes para un día específico para un usuario.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas.
     * @return El número de tareas para la fecha especificada.
     */
    override suspend fun getTaskCountForDate(userId: String, date: LocalDate): Int {
        return taskDao.getTaskCountForDate(userId, date)
    }

    /**
     * Cuenta el número de tareas completadas en un día específico para un usuario.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas completadas.
     * @return El número de tareas completadas en la fecha especificada.
     */
    override suspend fun getCompletedTaskCountForDate(userId: String, date: LocalDate): Int {
        return taskDao.getCompletedTaskCountForDate(userId, date)
    }
}