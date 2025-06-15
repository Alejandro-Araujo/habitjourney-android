package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar los detalles de un hábito existente.
 *
 * Esta clase encapsula la lógica para modificar un hábito, como su nombre,
 * descripción, frecuencia, etc., delegando la persistencia al repositorio.
 *
 * @property repository El repositorio de hábitos que se utilizará para guardar los cambios.
 */
class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para actualizar un hábito.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habit El objeto [Habit] que contiene los datos actualizados.
     */
    suspend operator fun invoke(habit: Habit) {
        repository.updateHabit(habit)
    }
}
