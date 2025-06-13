package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListItemUiModel

data class HabitsData(
    val allUserHabits: List<HabitListItemUiModel>,
    val todayHabits: List<HabitListItemUiModel>
)