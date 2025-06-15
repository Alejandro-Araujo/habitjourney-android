package com.alejandro.habitjourney.features.task.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Interfaz DAO (Data Access Object) para interactuar con la tabla 'tasks' en la base de datos.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas sobre las tareas.
 */
@Dao
interface TaskDao {

    /**
     * Inserta una nueva tarea en la base de datos o reemplaza una existente si hay conflicto.
     * @param task La entidad [TaskEntity] a insertar.
     * @return El ID de la fila insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    /**
     * Actualiza una tarea existente en la base de datos.
     * @param task La entidad [TaskEntity] a actualizar.
     */
    @Update
    suspend fun update(task: TaskEntity)

    /**
     * Obtiene una tarea por su ID.
     * @param taskId El ID de la tarea a buscar.
     * @return Un [Flow] que emite la [TaskEntity] correspondiente o `null` si no se encuentra.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<TaskEntity?>

    /**
     * Obtiene una tarea activa (no archivada) por su ID.
     * @param taskId El ID de la tarea a buscar.
     * @return Un [Flow] que emite la [TaskEntity] activa correspondiente o `null` si no se encuentra.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_archived = 0")
    fun getActiveTaskById(taskId: Long): Flow<TaskEntity?>

    /**
     * Obtiene todas las tareas activas (no completadas y no archivadas) para un usuario específico.
     * Las tareas se ordenan por fecha de vencimiento (nulas al final) y luego por prioridad.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [TaskEntity] activas.
     */
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_archived = 0
        ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, 
                 CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC
    """)
    fun getActiveTasks(userId: Long): Flow<List<TaskEntity>>

    /**
     * Obtiene todas las tareas completadas para un usuario específico.
     * Las tareas se ordenan por fecha de finalización de forma descendente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [TaskEntity] completadas.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 0 
        AND is_completed = 1
        ORDER BY completion_date DESC 
    """)
    fun getCompletedTasks(userId: Long): Flow<List<TaskEntity>>

    /**
     * Obtiene todas las tareas archivadas para un usuario específico.
     * Las tareas se ordenan por fecha de creación de forma descendente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de [TaskEntity] archivadas.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 1
        ORDER BY created_at DESC 
    """)
    fun getArchivedTasks(userId: Long): Flow<List<TaskEntity>>

    /**
     * Obtiene todas las tareas vencidas (no completadas, no archivadas y con fecha de vencimiento anterior a la actual)
     * para un usuario específico.
     * Las tareas se ordenan por fecha de vencimiento de forma ascendente.
     * @param userId El ID del usuario.
     * @param currentDate La fecha actual para comparar con la fecha de vencimiento.
     * @return Un [Flow] que emite una lista de [TaskEntity] vencidas.
     */
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_archived = 0
        AND due_date < :currentDate
        ORDER BY due_date ASC
    """)
    fun getOverdueTasks(userId: Long, currentDate: LocalDate): Flow<List<TaskEntity>>

    /**
     * Obtiene todas las tareas no archivadas para un usuario específico.
     * Las tareas se ordenan por fecha de creación de forma descendente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite una lista de todas las [TaskEntity] no archivadas.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 0 
        ORDER BY created_at DESC
    """)
    fun getAllTasks(userId: Long): Flow<List<TaskEntity>>

    /**
     * Marca una tarea como completada o incompleta y actualiza su fecha de finalización.
     * @param taskId El ID de la tarea a actualizar.
     * @param completed `true` para marcar como completada, `false` para marcar como incompleta.
     * @param completionDate La fecha de finalización si se marca como completada, o `null` si se marca como incompleta.
     */
    @Query("UPDATE tasks SET is_completed = :completed, completion_date = :completionDate WHERE id = :taskId")
    suspend fun setCompleted(taskId: Long, completed: Boolean, completionDate: LocalDate?)

    /**
     * Archiva una tarea específica.
     * @param taskId El ID de la tarea a archivar.
     */
    @Query("UPDATE tasks SET is_archived = 1 WHERE id = :taskId")
    suspend fun archiveTask(taskId: Long)

    /**
     * Desarchiva una tarea específica.
     * @param taskId El ID de la tarea a desarchivar.
     */
    @Query("UPDATE tasks SET is_archived = 0 WHERE id = :taskId")
    suspend fun unarchiveTask(taskId: Long)

    /**
     * Realiza una búsqueda de tareas no archivadas por título para un usuario específico.
     * La búsqueda es insensible a mayúsculas/minúsculas y busca coincidencias parciales.
     * @param userId El ID del usuario.
     * @param searchQuery El texto a buscar en los títulos de las tareas.
     * @return Un [Flow] que emite una lista de [TaskEntity] que coinciden con la búsqueda.
     */
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_archived = 0
        AND title LIKE '%' || :searchQuery || '%'
        ORDER BY created_at DESC
    """)
    fun searchTasks(userId: Long, searchQuery: String): Flow<List<TaskEntity>>

    /**
     * Obtiene tareas no archivadas para un usuario específico filtradas por prioridad.
     * Las tareas se ordenan por fecha de vencimiento de forma ascendente.
     * @param userId El ID del usuario.
     * @param priority La prioridad de las tareas a buscar (e.g., "HIGH", "MEDIUM", "LOW").
     * @return Un [Flow] que emite una lista de [TaskEntity] con la prioridad especificada.
     */
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_archived = 0
        AND priority = :priority
        ORDER BY due_date ASC
    """)
    fun getTasksByPriority(userId: Long, priority: String): Flow<List<TaskEntity>>

    /**
     * Elimina una tarea específica de la base de datos.
     * @param taskId El ID de la tarea a eliminar.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    /**
     * Obtiene una tarea por su ID de forma síncrona.
     * @param taskId El ID de la tarea a buscar.
     * @return La [TaskEntity] correspondiente o `null` si no se encuentra.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId  ")
    suspend fun getTaskByIdSync(taskId: Long): TaskEntity?

    /**
     * Obtiene una tarea activa (no archivada) por su ID de forma síncrona.
     * @param taskId El ID de la tarea a buscar.
     * @return La [TaskEntity] activa correspondiente o `null` si no se encuentra.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_archived = 0")
    suspend fun getActiveTaskByIdSync(taskId: Long): TaskEntity?

    /**
     * Obtiene las tareas completadas para un usuario y una fecha específica.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se buscan las tareas completadas.
     * @return Un [Flow] que emite una lista de [TaskEntity] completadas en la fecha dada.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_completed = 1 
        AND completion_date = :date
        ORDER BY completion_date DESC
    """)
    fun getCompletedTasksByDate(userId: Long, date: LocalDate): Flow<List<TaskEntity>>

    /**
     * Obtiene las tareas relevantes para una fecha específica para un usuario.
     * Incluye tareas con fecha de vencimiento en la fecha dada, completadas en la fecha dada,
     * o tareas sin fecha de vencimiento creadas antes o en la fecha dada.
     * Las tareas archivadas son excluidas.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se buscan las tareas.
     * @return Un [Flow] que emite una lista de [TaskEntity] relevantes para la fecha.
     */
    @Query("""
    SELECT * FROM tasks 
    WHERE user_id = :userId 
    AND is_archived = 0 
    AND (
        due_date = :date 
        OR completion_date = :date
        OR (due_date IS NULL AND DATE(created_at/1000, 'unixepoch') <= :date)
    )
    ORDER BY due_date ASC, priority ASC
""")
    fun getTasksForDate(userId: Long, date: LocalDate): Flow<List<TaskEntity>>

    /**
     * Cuenta el número de tareas programadas o relevantes para un día específico para un usuario.
     * Incluye tareas con fecha de vencimiento en la fecha dada, completadas en la fecha dada,
     * o tareas sin fecha de vencimiento creadas antes o en la fecha dada.
     * Las tareas archivadas son excluidas.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas.
     * @return El número de tareas para la fecha especificada.
     */
    @Query("""
    SELECT COUNT(*) FROM tasks 
    WHERE user_id = :userId 
    AND is_archived = 0 
    AND (
        due_date = :date 
        OR completion_date = :date
        OR (due_date IS NULL AND DATE(created_at/1000, 'unixepoch') <= :date)
    )
""")
    suspend fun getTaskCountForDate(userId: Long, date: LocalDate): Int

    /**
     * Cuenta el número de tareas completadas en un día específico para un usuario.
     * Las tareas archivadas son excluidas.
     * @param userId El ID del usuario.
     * @param date La fecha para la que se cuentan las tareas completadas.
     * @return El número de tareas completadas en la fecha especificada.
     */
    @Query("""
    SELECT COUNT(*) FROM tasks 
    WHERE user_id = :userId 
    AND is_archived = 0 
    AND is_completed = 1
    AND completion_date = :date
""")
    suspend fun getCompletedTaskCountForDate(userId: Long, date: LocalDate): Int
}