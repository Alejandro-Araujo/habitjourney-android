package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener un único hábito activo por su identificador.
 *
 * Esta clase se encarga de la lógica para recuperar un hábito específico,
 * asegurando que solo se devuelvan los hábitos que no están archivados.
 *
 * @property repository El repositorio de hábitos desde donde se obtendrá el dato.
 */
class GetHabitByIdUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para obtener un hábito por su ID.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habitId El ID único del hábito que se desea obtener.
     * @return El [Habit] correspondiente si se encuentra y está activo, o `null` en caso contrario.
     */
    suspend operator fun invoke(habitId: Long): Habit? {
        return repository.getHabitById(habitId)
    }
}
