package com.alejandro.habitjourney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface TaskDao {

    // Insertar tarea
    @Insert
    suspend fun insert(task: TaskEntity): Long

    // Obtener tareas activas para el usuario
    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_deleted = 0
        ORDER BY due_date ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveTasksPaged(userId: Long, limit: Int, offset: Int): List<TaskEntity>


    // Obtener tareas completadas
    @Query("""
    SELECT * FROM tasks 
    WHERE user_id = :userId 
    AND is_deleted = 0 
    AND is_completed = :completed
    ORDER BY due_date ASC
    LIMIT :limit OFFSET :offset
""")
    fun getCompletedTasks(userId: Long, completed: Boolean, limit: Int, offset: Int): Flow<List<TaskEntity>>

    // Obtener todas las tareas
    @Query("""
    SELECT * FROM tasks 
    WHERE user_id = :userId
    AND is_deleted = 0
    ORDER BY due_date ASC
    LIMIT :limit OFFSET :offset
""")
    fun getTasks(userId: Long, limit: Int, offset: Int): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND is_deleted = 0
        AND due_date < :currentDate
        ORDER BY due_date ASC
        LIMIT :limit OFFSET :offset
    """)
    fun getOverdueTasks(userId: Long, currentDate: LocalDate, limit: Int, offset: Int): Flow<List<TaskEntity>>

    // Actualizar tarea
    @Update
    suspend fun update(task: TaskEntity)

    // Marcar tarea como completada
    @Query("UPDATE tasks SET is_completed = :completed WHERE id = :taskId")
    suspend fun setCompleted(taskId: Long, completed: Boolean)

    // Eliminar tarea (soft delete)
    @Query("UPDATE tasks SET is_deleted = 1 WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)
}