package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los hábitos de un usuario, incluyendo los activos y archivados.
 *
 * Esta clase encapsula la lógica para solicitar la lista completa de hábitos de un usuario,
 * proporcionando un flujo (`Flow`) que se actualiza con cualquier cambio en los datos.
 *
 * @property habitRepository El repositorio desde donde se obtendrán los datos de los hábitos.
 */
class GetAllUserHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para obtener todos los hábitos del usuario.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyos hábitos se van a recuperar.
     * @return Un [Flow] que emite la lista completa de [Habit] del usuario.
     */
    operator fun invoke(userId: Long): Flow<List<Habit>> {
        return habitRepository.getAllHabitsForUser(userId)
    }
}
