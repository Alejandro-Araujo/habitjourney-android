package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

class ToggleTaskCompletionUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) {
        val completionDate = if (isCompleted) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        } else null

        taskRepository.setCompleted(taskId, isCompleted, completionDate)
    }
}