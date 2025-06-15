package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener un hábito junto con su historial completo de registros.
 *
 * Esta clase se encarga de la lógica para solicitar un [Habit] y todos sus [HabitLog] asociados,
 * empaquetándolos en un solo objeto [HabitWithLogs]. Proporciona un flujo de datos (`Flow`)
 * para que la UI pueda observar cambios en el hábito o en sus registros.
 *
 * @property repository El repositorio de hábitos desde donde se obtendrán los datos.
 */
class GetHabitWithLogsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para obtener el hábito con sus registros.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habitId El ID del hábito que se desea obtener.
     * @return Un [Flow] que emite el objeto [HabitWithLogs] correspondiente.
     */
    operator fun invoke(habitId: Long): Flow<HabitWithLogs> {
        return repository.getHabitWithLogs(habitId)
    }
}
