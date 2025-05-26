package com.alejandro.habitjourney.features.habit.domain.usecase


import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabitWithLogsUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(habitId: Long): Flow<HabitWithLogs> {
        return repository.getHabitWithLogs(habitId)
    }
}
