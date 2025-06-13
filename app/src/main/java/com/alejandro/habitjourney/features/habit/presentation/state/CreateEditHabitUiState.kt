package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate


data class CreateEditHabitUiState(
    val habitId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val type: HabitType = HabitType.DO,
    val frequency: String = "daily",
    val frequencyDays: List<Weekday> = emptyList(),
    val dailyTarget: Int? = 1,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = 0L
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                (dailyTarget != null && dailyTarget > 0) &&
                (frequency != "weekly" || frequencyDays.isNotEmpty())
}