package com.alejandro.habitjourney.features.task.domain.repository


import kotlinx.datetime.LocalDateTime

interface ReminderRepository {
    suspend fun scheduleReminder(taskId: Long, dateTime: LocalDateTime, title: String)
    suspend fun cancelReminder(taskId: Long)
    suspend fun updateReminder(taskId: Long, newDateTime: LocalDateTime, title: String)
    suspend fun isReminderScheduled(taskId: Long): Boolean
}