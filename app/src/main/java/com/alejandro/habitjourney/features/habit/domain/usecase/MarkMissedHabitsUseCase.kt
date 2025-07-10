package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Use case para identificar y marcar como MISSED los hábitos que debían completarse
 * en una fecha determinada pero no tienen un registro de COMPLETED o SKIPPED.
 *
 * Esta es una operación de "mantenimiento" que no se activa directamente por la UI del usuario,
 * sino por una lógica de fondo o al inicio de la aplicación para procesar días pasados.
 */
class MarkMissedHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(userId: String, date: LocalDate) {

        val habitsDueOnDate = repository.getHabitsDueTodayWithCompletionCount(userId, date, date.dayOfWeek.ordinal).firstOrNull()?.map { it.first } ?: emptyList()


        for (habit in habitsDueOnDate) {
            val existingLog = repository.getLogForDate(habit.id, date).firstOrNull()

            if (existingLog == null || (existingLog.status != LogStatus.COMPLETED && existingLog.status != LogStatus.SKIPPED)) {
                val missedLog = existingLog?.copy(
                    value = 0f,
                    status = LogStatus.MISSED
                ) ?: HabitLog(
                    habitId = habit.id,
                    date = date,
                    value = 0f,
                    status = LogStatus.MISSED
                )
                repository.logHabitCompletion(missedLog)
            }
        }
    }
}