package com.alejandro.habitjourney.features.task.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.task.domain.usecase.*
import com.alejandro.habitjourney.features.task.presentation.state.TaskFilterType
import com.alejandro.habitjourney.features.task.presentation.state.TaskListUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val getCompletedTasksUseCase: GetCompletedTasksUseCase,
    private val getArchivedTasksUseCase: GetArchivedTasksUseCase,
    private val getOverdueTasksUseCase: GetOverdueTasksUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilterType.ACTIVE)
    val currentFilter: StateFlow<TaskFilterType> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority: StateFlow<Priority?> = _selectedPriority.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _isSearchActive = MutableStateFlow(false)
    private val _isFilterDropdownExpanded = MutableStateFlow(false)

    private val _baseUiState = combine(
        _currentFilter,
        _searchQuery,
        _selectedPriority,
        _isLoading,
        _error
    ) { filter, search, priority, loading, error ->
        TaskListUiState(
            currentFilter = filter,
            searchQuery = search,
            selectedPriority = priority,
            isLoading = loading,
            error = error
        )
    }

    val uiState: StateFlow<TaskListUiState> = combine(
        _baseUiState,
        _isSearchActive,
    ) { baseState, searchActive ->
        baseState.copy(
            isSearchActive = searchActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<List<Task>> =
        combine(
            _currentFilter,
            userPreferences.getCurrentUserId()
        ) { filter, userId ->
            if (userId != null) {
                when (filter) {
                    TaskFilterType.ACTIVE -> getActiveTasksUseCase(userId)
                    TaskFilterType.COMPLETED -> getCompletedTasksUseCase(userId)
                    TaskFilterType.ARCHIVED -> getArchivedTasksUseCase(userId)
                    TaskFilterType.OVERDUE -> getOverdueTasksUseCase(userId)
                    TaskFilterType.ALL ->  getAllTasksUseCase(userId)
                }
            } else {
                flowOf(emptyList())
            }
        }.flatMapLatest { it }
            .combine(_searchQuery) { taskList, query ->
                if (query.isBlank()) {
                    taskList
                } else {
                    taskList.filter { task ->
                        task.title.contains(query, ignoreCase = true) ||
                                task.description?.contains(query, ignoreCase = true) == true
                    }
                }
            }
            .combine(_selectedPriority) { taskList, priority ->
                if (priority == null) {
                    taskList
                } else {
                    taskList.filter { it.priority == priority }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun setFilter(filter: TaskFilterType) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }


    fun setPriorityFilter(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                toggleTaskCompletionUseCase(taskId, isCompleted)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun archiveTask(taskId: Long) {
        viewModelScope.launch {
            try {
                archiveTaskUseCase(taskId, true)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch {
            try {
                archiveTaskUseCase(taskId, false)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                deleteTaskUseCase(taskId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}