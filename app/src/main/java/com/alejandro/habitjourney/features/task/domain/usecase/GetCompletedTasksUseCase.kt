package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCompletedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: Long): Flow<List<Task>> {
        return taskRepository.getCompletedTasks(userId)
    }
}