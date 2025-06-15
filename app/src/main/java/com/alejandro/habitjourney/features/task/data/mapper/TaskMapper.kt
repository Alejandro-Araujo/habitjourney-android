package com.alejandro.habitjourney.features.task.data.mapper

import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.task.domain.model.Task

/**
 * Objeto de mapeo que proporciona funciones de extensión para convertir entre
 * las entidades de datos (TaskEntity) y los modelos de dominio (Task).
 * Esto facilita la separación de las capas de datos y dominio,
 * asegurando que la lógica de negocio opere con modelos limpios
 * y la persistencia maneje las entidades de base de datos.
 */
object TaskMapper {

    /**
     * Convierte un objeto del modelo de dominio [Task] a su correspondiente entidad de base de datos [TaskEntity].
     * Esta función de extensión permite llamar a `toEntity()` directamente sobre una instancia de [Task].
     *
     * @receiver La instancia de [Task] que se va a convertir.
     * @return Una nueva instancia de [TaskEntity] con los datos mapeados desde la [Task].
     */
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

    /**
     * Convierte una entidad de base de datos [TaskEntity] a su correspondiente objeto del modelo de dominio [Task].
     * Esta función de extensión permite llamar a `toDomain()` directamente sobre una instancia de [TaskEntity].
     *
     * @receiver La instancia de [TaskEntity] que se va a convertir.
     * @return Una nueva instancia de [Task] con los datos mapeados desde la [TaskEntity].
     */
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

    /**
     * Convierte una lista de entidades de base de datos [TaskEntity] a una lista de objetos del modelo de dominio [Task].
     * Utiliza la función `toDomain()` individual para cada elemento de la lista.
     *
     * @receiver La lista de instancias de [TaskEntity] que se van a convertir.
     * @return Una nueva [List] de [Task] con los datos mapeados.
     */
    fun List<TaskEntity>.toDomain(): List<Task> = map { it.toDomain() }
}