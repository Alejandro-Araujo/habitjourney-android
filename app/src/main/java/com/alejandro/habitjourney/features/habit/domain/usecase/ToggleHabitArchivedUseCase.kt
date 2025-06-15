package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import javax.inject.Inject

/**
 * Caso de uso para cambiar el estado de archivado de un hábito.
 *
 * Esta clase encapsula la lógica para archivar o desarchivar un hábito,
 * permitiendo ocultarlo de las vistas principales sin eliminarlo permanentemente.
 *
 * @property habitRepository El repositorio que se utilizará para actualizar el estado del hábito.
 */
class ToggleHabitArchivedUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * Ejecuta el caso de uso para archivar o desarchivar un hábito.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param habitId El ID del hábito a modificar.
     * @param archive `true` para archivar el hábito, `false` para desarchivarlo.
     */
    suspend operator fun invoke(habitId: Long, archive: Boolean) {
        habitRepository.toggleHabitArchived(habitId, archive)
    }
}
