package com.alejandro.habitjourney.features.task.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_archived = 0")
    fun getActiveTaskById(taskId: Long): Flow<TaskEntity?>

    // Obtener tareas activas ordenadas por fecha y prioridad
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_archived = 0
        ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, 
                 CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC
    """)
    fun getActiveTasks(userId: Long): Flow<List<TaskEntity>>

    // Obtener tareas completadas
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 0 
        AND is_completed = 1
        ORDER BY completion_date DESC 
    """)
    fun getCompletedTasks(userId: Long): Flow<List<TaskEntity>>

    // Obtener tareas archivadas
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 1
        ORDER BY created_at DESC 
    """)
    fun getArchivedTasks(userId: Long): Flow<List<TaskEntity>>

    // Obtener tareas vencidas
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_archived = 0
        AND due_date < :currentDate
        ORDER BY due_date ASC
    """)
    fun getOverdueTasks(userId: Long, currentDate: LocalDate): Flow<List<TaskEntity>>

    // Todas las tareas no archivadas
    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_archived = 0 
        ORDER BY created_at DESC
    """)
    fun getAllTasks(userId: Long): Flow<List<TaskEntity>>

    // Marcar tarea como completada/incompleta
    @Query("UPDATE tasks SET is_completed = :completed, completion_date = :completionDate WHERE id = :taskId")
    suspend fun setCompleted(taskId: Long, completed: Boolean, completionDate: LocalDate?)

    // Archivar tarea
    @Query("UPDATE tasks SET is_archived = 1 WHERE id = :taskId")
    suspend fun archiveTask(taskId: Long)

    // Desarchivar tarea
    @Query("UPDATE tasks SET is_archived = 0 WHERE id = :taskId")
    suspend fun unarchiveTask(taskId: Long)

    // Búsqueda por título (útil para filtros)
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_archived = 0
        AND title LIKE '%' || :searchQuery || '%'
        ORDER BY created_at DESC
    """)
    fun searchTasks(userId: Long, searchQuery: String): Flow<List<TaskEntity>>

    // Tareas por prioridad
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_archived = 0
        AND priority = :priority
        ORDER BY due_date ASC
    """)
    fun getTasksByPriority(userId: Long, priority: String): Flow<List<TaskEntity>>


    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT * FROM tasks WHERE id = :taskId  ")
    suspend fun getTaskByIdSync(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :taskId AND is_archived = 0")
    suspend fun getActiveTaskByIdSync(taskId: Long): TaskEntity?

    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND is_completed = 1 
        AND completion_date = :date
        ORDER BY completion_date DESC
    """)
    fun getCompletedTasksByDate(userId: Long, date: LocalDate): Flow<List<TaskEntity>>
}