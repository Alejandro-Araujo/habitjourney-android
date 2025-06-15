package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para obtener el registro de un hábito para una fecha específica.
 *
 * Esta clase encapsula la lógica para solicitar un único [HabitLog] basado en el ID
 * del hábito y la fecha. Proporciona un flujo de datos (`Flow`) para que la UI
 * pueda reaccionar a los cambios en el registro de ese día.
 *
 * @property habitRepository El repositorio desde donde se obtendrán los datos.
 */
class GetLogForDateUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para obtener el registro de una fecha.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habitId El ID del hábito cuyo registro se desea obtener.
     * @param date La fecha específica del registro a consultar.
     * @return Un [Flow] que emite el [HabitLog] para la fecha dada, o `null` si no existe.
     */
    operator fun invoke(habitId: Long, date: LocalDate): Flow<HabitLog?> {
        return habitRepository.getLogForDate(habitId, date)
    }
}
