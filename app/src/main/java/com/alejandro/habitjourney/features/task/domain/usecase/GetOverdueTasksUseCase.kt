package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

class GetOverdueTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userId: Long): Flow<List<Task>> {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return taskRepository.getOverdueTasks(userId, currentDate)
    }
}