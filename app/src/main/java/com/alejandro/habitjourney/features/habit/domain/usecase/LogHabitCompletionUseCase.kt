package com.alejandro.habitjourney.features.habit.domain.usecase


import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class LogHabitCompletionUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: Long,
        date: LocalDate,
        value: Float
    ) {
        // Determinar el estado basado en el valor
        val status = if (value > 0) LogStatus.COMPLETED else LogStatus.NOT_COMPLETED

        habitRepository.logHabitCompletion(
            habitId = habitId,
            date = date,
            value = value,
            status = status
        )
    }
}