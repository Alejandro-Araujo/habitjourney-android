package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import javax.inject.Inject

class ToggleHabitArchivedUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long, archive: Boolean) {
        habitRepository.toggleHabitArchived(habitId, archive)
    }
}