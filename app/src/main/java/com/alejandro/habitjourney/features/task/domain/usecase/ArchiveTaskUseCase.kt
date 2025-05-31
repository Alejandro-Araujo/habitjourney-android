package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject

class ArchiveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isArchived: Boolean = true) {
        taskRepository.archiveTask(taskId, isArchived)
    }
}