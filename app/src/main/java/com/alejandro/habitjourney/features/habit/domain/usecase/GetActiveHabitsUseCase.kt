package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) {

    operator fun invoke(userId: Long): Flow<List<Habit>> {
        return repository.getActiveHabitsForUser(userId)
    }
}