package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.features.habit.presentation.viewmodel.HabitListItemUiModel

data class HabitListUiState(
    val todayHabits: List<HabitListItemUiModel> = emptyList(),
    val filteredHabits: List<HabitListItemUiModel> = emptyList(),
    val currentFilter: HabitFilterType = HabitFilterType.TODAY,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

