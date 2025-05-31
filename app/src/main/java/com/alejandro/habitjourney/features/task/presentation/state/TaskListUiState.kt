package com.alejandro.habitjourney.features.task.presentation.state


import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.domain.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: TaskFilterType = TaskFilterType.ACTIVE,
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val isSearchActive: Boolean = false,
    val isFilterDropdownExpanded: Boolean = false
)
