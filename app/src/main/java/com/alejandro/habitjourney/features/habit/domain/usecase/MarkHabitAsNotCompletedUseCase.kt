package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

class MarkHabitAsNotCompletedUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getLogForDateUseCase: GetLogForDateUseCase // Necesitamos el log actual
) {
    suspend operator fun invoke(habitId: Long) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val existingLog = getLogForDateUseCase(habitId, today).firstOrNull()

        if (existingLog != null) {
            // Actualizar el log existente a NOT_COMPLETED
            val updatedLog = existingLog.copy(
                status = LogStatus.NOT_COMPLETED,
                value = 0f // Resetear el progreso del log para hoy también
            )
            habitRepository.updateHabitLog(updatedLog)
        } else {
            // Si no hay log, no hay nada que deshacer (o podrías crear uno en NOT_COMPLETED)
            // Por ahora, no hacemos nada si no hay log para hoy.
        }
    }
}