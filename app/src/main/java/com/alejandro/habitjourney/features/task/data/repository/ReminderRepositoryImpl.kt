package com.alejandro.habitjourney.features.task.data.repository


import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import kotlinx.datetime.LocalDateTime
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderManager: ReminderManager
) : ReminderRepository {

    override suspend fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String) {
        reminderManager.scheduleReminder(taskId, dateTime, title)
    }

    override suspend fun cancelReminder(taskId: Long) {
        reminderManager.cancelReminder(taskId)
    }

    override suspend fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String) {
        reminderManager.updateReminder(taskId, newDateTime, title)
    }

    override suspend fun isReminderScheduled(taskId: Long): Boolean {
        return reminderManager.isReminderScheduled(taskId)
    }
}