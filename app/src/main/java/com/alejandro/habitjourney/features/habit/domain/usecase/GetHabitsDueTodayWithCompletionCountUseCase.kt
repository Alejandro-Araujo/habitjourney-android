package com.alejandro.habitjourney.features.habit.domain.usecase


import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetHabitsDueTodayWithCompletionCountUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    operator fun invoke(userId: Long, today: LocalDate, weekdayIndex: Int): Flow<List<Pair<Habit, Int>>> {
        return repository.getHabitsDueTodayWithCompletionCount(userId, today, weekdayIndex)
    }
}