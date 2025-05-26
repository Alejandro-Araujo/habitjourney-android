package com.alejandro.habitjourney.features.habit.data.mapper

import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import javax.inject.Inject

class HabitLocalMapper @Inject constructor() {

    fun habitEntityToDomain(entity: HabitEntity): Habit {
        return Habit(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            frequency = entity.frequency,
            frequencyDays = entity.frequencyDays,
            dailyTarget = entity.dailyTarget,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isArchived = entity.isArchived,
            createdAt = entity.createdAt
        )
    }

    fun habitDomainToEntity(domain: Habit): HabitEntity {
        return HabitEntity(
            id = domain.id,
            userId = domain.userId,
            name = domain.name,
            description = domain.description,
            type = domain.type,
            frequency = domain.frequency,
            frequencyDays = domain.frequencyDays,
            dailyTarget = domain.dailyTarget,
            startDate = domain.startDate,
            endDate = domain.endDate,
            isArchived = domain.isArchived,
            createdAt = domain.createdAt
        )
    }

    fun habitLogEntityToDomain(entity: HabitLogEntity): HabitLog {
        return HabitLog(
            id = entity.id,
            habitId = entity.habitId,
            date = entity.date,
            status = entity.status,
            value = entity.value,
            createdAt = entity.createdAt
        )
    }

    fun habitLogDomainToEntity(domain: HabitLog): HabitLogEntity {
        return HabitLogEntity(
            id = domain.id,
            habitId = domain.habitId,
            date = domain.date,
            status = domain.status,
            value = domain.value,
            createdAt = domain.createdAt
        )
    }

    // Si tienes listas
    fun habitEntityListToDomain(entities: List<HabitEntity>): List<Habit> {
        return entities.map { habitEntityToDomain(it) }
    }

    fun habitLogEntityListToDomain(entities: List<HabitLogEntity>): List<HabitLog> {
        return entities.map { habitLogEntityToDomain(it) }
    }
}