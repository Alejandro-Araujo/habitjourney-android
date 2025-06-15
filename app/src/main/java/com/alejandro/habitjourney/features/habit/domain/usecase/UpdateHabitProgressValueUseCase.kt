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
 * Caso de uso para actualizar el valor de progreso numérico de un hábito.
 *
 * Esta clase maneja la lógica para incrementar o decrementar el progreso de un hábito contable
 * en una fecha específica. Determina el estado final ([LogStatus]) basado en si el nuevo
 * valor alcanza el objetivo diario del hábito.
 *
 * @property repository El repositorio de hábitos para obtener datos y persistir los cambios.
 */
class UpdateHabitProgressValueUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para actualizar el progreso.
     *
     * @param habitId El ID del hábito a actualizar.
     * @param valueDelta La cantidad a sumar o restar al progreso actual (ej: 1f para sumar, -1f para restar).
     * @param date La fecha para la que se actualiza el progreso. Por defecto, es el día de hoy.
     * @throws IllegalArgumentException si no se encuentra el hábito con el ID proporcionado.
     */
    suspend operator fun invoke(
        habitId: Long,
        valueDelta: Float,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {
        val habit = repository.getHabitById(habitId)
            ?: throw IllegalArgumentException("Hábito con ID $habitId no encontrado para actualizar progreso.")

        val existingLog = repository.getLogForDate(habitId, date).firstOrNull()
        val currentValue = existingLog?.value ?: 0f

        // Calcula el nuevo valor, asegurando que no sea negativo.
        val newValue = (currentValue + valueDelta).coerceAtLeast(0f)

        // Determina el estado final basado en el objetivo y el nuevo valor.
        val finalStatus = if (habit.dailyTarget != null && habit.dailyTarget > 0) {
            when {
                newValue >= habit.dailyTarget -> LogStatus.COMPLETED
                newValue > 0f -> LogStatus.PARTIAL // Estado intermedio si no se ha completado pero hay progreso.
                else -> LogStatus.NOT_COMPLETED
            }
        } else {
            // Para hábitos sin objetivo (Sí/No), se completa si el valor es mayor que 0.
            if (newValue > 0f) LogStatus.COMPLETED else LogStatus.NOT_COMPLETED
        }

        val updatedLog = existingLog?.copy(
            value = newValue,
            status = finalStatus
        ) ?: HabitLog( // Crea un nuevo log si no existía.
            habitId = habitId,
            date = date,
            value = newValue,
            status = finalStatus
        )

        repository.logHabitCompletion(updatedLog)
    }
}
