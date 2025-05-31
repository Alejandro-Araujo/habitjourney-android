package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class UpdateReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(taskId: Long, newDateTime: LocalDateTime, title: String) {
        reminderRepository.updateReminder(taskId, newDateTime, title)
    }
}
