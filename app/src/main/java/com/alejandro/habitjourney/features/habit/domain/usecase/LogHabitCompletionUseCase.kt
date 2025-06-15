package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para registrar la finalización o el progreso de un hábito.
 *
 * Esta clase encapsula la lógica de negocio para crear o actualizar un registro de hábito.
 * Determina automáticamente el estado ([LogStatus.COMPLETED] o [LogStatus.NOT_COMPLETED])
 * en función del valor de progreso proporcionado.
 *
 * @property habitRepository El repositorio de hábitos que se utilizará para persistir el registro.
 */
class LogHabitCompletionUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para registrar el progreso de un hábito.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habitId El ID del hábito cuyo progreso se va a registrar.
     * @param date La fecha para la que se está registrando el progreso.
     * @param value El valor del progreso (ej: 1.0 para hábitos de SÍ/NO, o un número para hábitos contables).
     */
    suspend operator fun invoke(
        habitId: Long,
        date: LocalDate,
        value: Float
    ) {
        val status = if (value > 0) LogStatus.COMPLETED else LogStatus.NOT_COMPLETED

        habitRepository.logHabitCompletion(
            habitId = habitId,
            date = date,
            value = value,
            status = status
        )
    }
}
