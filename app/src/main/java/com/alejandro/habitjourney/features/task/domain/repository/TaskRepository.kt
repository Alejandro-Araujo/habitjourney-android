package com.alejandro.habitjourney.features.task.domain.repository


import com.alejandro.habitjourney.features.task.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TaskRepository {
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long)
    fun getTaskById(taskId: Long): Flow<Task?>
    fun getActiveTasks(userId: Long): Flow<List<Task>>
    fun getCompletedTasks(userId: Long): Flow<List<Task>>
    fun getArchivedTasks(userId: Long): Flow<List<Task>>
    fun getOverdueTasks(userId: Long, currentDate: LocalDate): Flow<List<Task>>
    fun getAllTasks(userId: Long): Flow<List<Task>>
    suspend fun setCompleted(taskId: Long, isCompleted: Boolean, completionDate: LocalDate?)
    suspend fun archiveTask(taskId: Long, isArchived: Boolean)
}