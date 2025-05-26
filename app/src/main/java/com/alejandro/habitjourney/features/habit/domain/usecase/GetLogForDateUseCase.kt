// com.alejandro.habitjourney.features.habit.domain.usecase.GetLogForDateUseCase.kt
package com.alejandro.habitjourney.features.habit.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetLogForDateUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(habitId: Long, date: LocalDate): Flow<HabitLog?> {
        return habitRepository.getLogForDate(habitId, date)
    }
}