package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener una lista de todos los hábitos activos de un usuario.
 *
 * Esta clase se encarga de la lógica para recuperar solo los hábitos que no están archivados,
 * proporcionando un flujo de datos (`Flow`) que se actualiza automáticamente
 * cuando cambian los datos subyacentes en la base de datos.
 *
 * @property repository El repositorio de hábitos desde donde se obtendrán los datos.
 */
class GetActiveHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para obtener los hábitos activos.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyos hábitos activos se quieren obtener.
     * @return Un [Flow] que emite una lista de [Habit] activos para el usuario especificado.
     */
    operator fun invoke(userId: String): Flow<List<Habit>> {
        return repository.getActiveHabitsForUser(userId)
    }
}
