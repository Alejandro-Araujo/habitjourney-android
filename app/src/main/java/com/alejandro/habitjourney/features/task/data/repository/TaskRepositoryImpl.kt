package com.alejandro.habitjourney.features.task.data.repository

import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.task.data.mapper.TaskMapper.toDomain
import com.alejandro.habitjourney.features.task.data.mapper.TaskMapper.toEntity
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(task: Task): Long {
        val taskId = taskDao.insert(task.toEntity())
        return taskId
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.toEntity())
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }

    override fun getTaskById(taskId: Long): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomain() }
    }

    override fun getActiveTasks(userId: Long): Flow<List<Task>> {
        return taskDao.getActiveTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    override fun getCompletedTasks(userId: Long): Flow<List<Task>> {
        return taskDao.getCompletedTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    override fun getArchivedTasks(userId: Long): Flow<List<Task>> {
        return taskDao.getArchivedTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    override fun getOverdueTasks(userId: Long, currentDate: LocalDate): Flow<List<Task>> {
        return taskDao.getOverdueTasks(userId, currentDate).map { entities ->
            entities.toDomain()
        }
    }

    override fun getAllTasks(userId: Long): Flow<List<Task>> {
        return taskDao.getAllTasks(userId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun setCompleted(
        taskId: Long,
        isCompleted: Boolean,
        completionDate: LocalDate?
    ) {
        taskDao.setCompleted(taskId, isCompleted, completionDate)
    }

    override suspend fun archiveTask(taskId: Long, isArchived: Boolean) {
        if (isArchived) {
            taskDao.archiveTask(taskId)
        } else {
            taskDao.unarchiveTask(taskId)
        }
    }
}