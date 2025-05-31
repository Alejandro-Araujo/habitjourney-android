package com.alejandro.habitjourney.features.task.domain.usecase


import android.util.Log
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import javax.inject.Inject


class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long {
        return taskRepository.insertTask(task)
    }
}