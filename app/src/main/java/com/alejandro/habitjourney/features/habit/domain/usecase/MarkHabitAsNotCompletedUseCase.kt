package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Caso de uso para marcar un hábito como no completado para el día de hoy.
 *
 * Esta clase se encarga de la lógica para deshacer una finalización. Si existe un registro
 * para el día de hoy, lo actualiza al estado [LogStatus.NOT_COMPLETED] y resetea su
 * valor de progreso a cero.
 *
 * @property habitRepository El repositorio para actualizar los datos del registro.
 * @property getLogForDateUseCase Caso de uso para obtener el registro existente para hoy.
 */
class MarkHabitAsNotCompletedUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getLogForDateUseCase: GetLogForDateUseCase
) {
    /**
     * Ejecuta el caso de uso.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     * Si no existe un registro para el día de hoy, no se realiza ninguna acción.
     *
     * @param habitId El ID del hábito que se va a marcar como no completado.
     */
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
            // Si no hay log, no hay nada que deshacer.
        }
    }
}
