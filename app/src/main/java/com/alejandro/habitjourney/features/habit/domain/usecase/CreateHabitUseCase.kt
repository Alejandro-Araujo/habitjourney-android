package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * Caso de uso para crear un nuevo hábito.
 *
 * Esta clase encapsula la lógica de negocio específica para la creación de un hábito,
 * actuando como intermediario entre la capa de presentación (ViewModel) y la capa de datos (Repository).
 *
 * @property repository El repositorio de hábitos que se utilizará para persistir los datos.
 */
class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para crear un hábito.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habit El modelo de dominio [Habit] que se va a crear.
     * @return El ID (`Long`) del hábito recién creado.
     */
    suspend operator fun invoke(habit: Habit): Long {
        return repository.createHabit(habit)
    }
}
