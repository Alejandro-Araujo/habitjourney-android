package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Use case para permitir al usuario marcar explícitamente un hábito como OMITIDO (SKIPPED) para una fecha.
 * Este estado no se deriva del progreso numérico y el valor del progreso se establece en 0.
 */
class MarkHabitAsSkippedUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: Long,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {

        val existingLog = repository.getLogForDate(habitId, date).firstOrNull()

        val valueToSet = 0f

        val updatedLog = existingLog?.copy(
            value = valueToSet,
            status = LogStatus.SKIPPED
        ) ?: HabitLog(
            habitId = habitId,
            date = date,
            value = valueToSet,
            status = LogStatus.SKIPPED
        )
        repository.logHabitCompletion(updatedLog)
    }
}