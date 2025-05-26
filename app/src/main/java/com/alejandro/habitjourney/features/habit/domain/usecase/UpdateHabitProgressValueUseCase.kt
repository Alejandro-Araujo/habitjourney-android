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
 * Use case para actualizar el valor numérico de progreso de un hábito en una fecha específica.
 * Determina el LogStatus (NOT_COMPLETED, PARTIAL, COMPLETED) basado en el valor acumulado y el dailyTarget.
 * Maneja tanto incrementos (botón de completar) como decrementos (botón de deshacer).
 */
class UpdateHabitProgressValueUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: Long,
        valueDelta: Float,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {
        val habit = repository.getHabitById(habitId)
            ?: throw IllegalArgumentException("Hábito con ID $habitId no encontrado para actualizar progreso.")

        val existingLog = repository.getLogForDate(habitId, date).firstOrNull()
        val currentValue = existingLog?.value ?: 0f

        val newValue = (currentValue + valueDelta).coerceAtLeast(0f)

        val finalStatus = if (habit.dailyTarget != null && habit.dailyTarget > 0) {
            when {
                newValue >= habit.dailyTarget -> LogStatus.COMPLETED
                newValue > 0f -> LogStatus.PARTIAL
                else -> LogStatus.NOT_COMPLETED
            }
        } else {
            if (newValue > 0f) LogStatus.COMPLETED else LogStatus.NOT_COMPLETED
        }

        val updatedLog = existingLog?.copy(
            value = newValue,
            status = finalStatus
        ) ?: HabitLog(
            habitId = habitId,
            date = date,
            value = newValue,
            status = finalStatus
        )

        repository.logHabitCompletion(updatedLog)
    }
}