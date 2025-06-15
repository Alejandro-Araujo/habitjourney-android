package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Caso de uso para obtener los hábitos programados para hoy junto con su progreso de completitud.
 *
 * Esta clase recupera los hábitos que, por su configuración de frecuencia, deben realizarse
 * en el día de la semana actual. Además, adjunta a cada hábito el número de veces
 * que ya ha sido completado en la fecha de hoy.
 *
 * @property repository El repositorio de hábitos desde donde se obtendrán los datos.
 */
class GetHabitsDueTodayWithCompletionCountUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario.
     * @param today La fecha exacta de hoy, usada para buscar los registros de completitud.
     * @param weekdayIndex El índice del día de la semana de hoy, usado para filtrar los hábitos por su frecuencia.
     * @return Un [Flow] que emite una lista de pares. Cada par contiene un [Habit]
     * y un [Int] que representa su conteo de completitud para el día de hoy.
     */
    operator fun invoke(userId: Long, today: LocalDate, weekdayIndex: Int): Flow<List<Pair<Habit, Int>>> {
        return repository.getHabitsDueTodayWithCompletionCount(userId, today, weekdayIndex)
    }
}
