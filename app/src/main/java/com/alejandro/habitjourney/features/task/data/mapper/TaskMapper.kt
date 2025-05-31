package com.alejandro.habitjourney.features.task.data.mapper


import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.task.domain.model.Task

object TaskMapper {

    fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            isCompleted = isCompleted,
            completionDate = completionDate,
            isArchived = isArchived,
            createdAt = createdAt,
            reminderDateTime = reminderDateTime,
            isReminderSet = isReminderSet
        )
    }

    fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            isCompleted = isCompleted,
            completionDate = completionDate,
            isArchived = isArchived,
            createdAt = createdAt,
            reminderDateTime = reminderDateTime,
            isReminderSet = isReminderSet
        )
    }

    fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }
}