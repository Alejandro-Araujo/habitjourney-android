package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import javax.inject.Inject

class CancelReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(taskId: Long) {
        reminderRepository.cancelReminder(taskId)
    }
}