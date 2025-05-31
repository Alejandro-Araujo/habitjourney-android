package com.alejandro.habitjourney.features.task.domain.usecase


import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(taskId: Long, dateTime: LocalDateTime, title: String) {
        reminderRepository.scheduleReminder(taskId, dateTime, title)
    }
}