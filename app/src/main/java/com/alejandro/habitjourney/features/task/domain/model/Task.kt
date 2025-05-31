package com.alejandro.habitjourney.features.task.domain.model


import com.alejandro.habitjourney.core.data.local.enums.Priority
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class Task(
    val id: Long = 0L,
    val userId: Long,
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val priority: Priority? = null,
    val isCompleted: Boolean = false,
    val completionDate: LocalDate? = null,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val reminderDateTime: LocalDateTime? = null,
    val isReminderSet: Boolean = false
)
