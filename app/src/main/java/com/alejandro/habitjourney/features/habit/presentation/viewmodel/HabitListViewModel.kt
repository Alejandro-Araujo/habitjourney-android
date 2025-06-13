package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.habit.domain.usecase.*
import com.alejandro.habitjourney.features.habit.presentation.screen.HabitIconMapper
import com.alejandro.habitjourney.features.habit.presentation.state.HabitFilterType
import com.alejandro.habitjourney.features.habit.presentation.state.HabitListUiState
import com.alejandro.habitjourney.features.habit.presentation.state.HabitsData
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject

@SuppressLint("StringFormatInvalid")
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getAllUserHabitsUseCase: GetAllUserHabitsUseCase,
    private val getHabitsDueTodayWithCompletionCountUseCase: GetHabitsDueTodayWithCompletionCountUseCase,
    private val updateHabitProgressValueUseCase: UpdateHabitProgressValueUseCase,
    private val markHabitAsSkippedUseCase: MarkHabitAsSkippedUseCase,
    private val getLogForDateUseCase: GetLogForDateUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val markHabitAsNotCompletedUseCase: MarkHabitAsNotCompletedUseCase,
    private val toggleHabitArchivedUseCase: ToggleHabitArchivedUseCase,
    private val markMissedHabitsUseCase: MarkMissedHabitsUseCase,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _currentFilter = MutableStateFlow(HabitFilterType.TODAY)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _refreshTrigger = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val habitsDataFlow = combine(
        userPreferences.userIdFlow,
        _refreshTrigger
    ) { userId, _ ->
        userId
    }.flatMapLatest { userId ->
        if (userId != null) {
            _isLoading.value = true
            _error.value = null

            val today = getCurrentLocalDate()
            val weekdayIndex = today.dayOfWeek.isoDayNumber

            combine(
                getAllUserHabitsUseCase(userId),
                getHabitsDueTodayWithCompletionCountUseCase(userId, today, weekdayIndex)
            ) { allUserHabits, todayHabitsWithCounts ->
                _isLoading.value = false

                val habitsWithReactiveLogs = allUserHabits.map { habit ->
                    getLogForDateUseCase(habit.id, today).map { todayLog ->
                        val todayCompletionCount = todayHabitsWithCounts
                            .find { it.first.id == habit.id }?.second ?: 0

                        habit.toHabitListItemUiModel(
                            currentCompletionCount = todayCompletionCount,
                            todayLog = todayLog,
                            icon = HabitIconMapper.getIconForHabitType(habit.type)
                        )
                    }
                }

                combine(habitsWithReactiveLogs) { habitUiModels ->
                    val todayHabitsWithCounts = todayHabitsWithCounts.map { it.first.id }.toSet()

                    HabitsData(
                        allUserHabits = habitUiModels.toList(),
                        todayHabits = habitUiModels.filter { habit ->
                            todayHabitsWithCounts.contains(habit.id)
                        }
                    )
                }
            }.flatMapLatest { it }
                .catch { exception ->
                    _error.value = exception.message ?: context.getString(R.string.error_unknown)
                    _isLoading.value = false
                    emit(HabitsData(emptyList(), emptyList()))
                }
        } else {
            _error.value = context.getString(R.string.error_user_not_logged_in)
            _isLoading.value = false
            flowOf(HabitsData(emptyList(), emptyList()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsData(emptyList(), emptyList())
    )

    val uiState: StateFlow<HabitListUiState> = combine(
        habitsDataFlow,
        _isLoading,
        _error,
        _currentFilter,
        _searchQuery,
        _isSearchActive
    ) { values ->
        val habitsData = values[0] as HabitsData
        val isLoading = values[1] as Boolean
        val error = values[2] as String?
        val currentFilter = values[3] as HabitFilterType
        val searchQuery = values[4] as String
        val isSearchActive = values[5] as Boolean

        val searchFiltered = if (searchQuery.isNotBlank()) {
            habitsData.allUserHabits.filter { habit ->
                habit.name.contains(searchQuery, ignoreCase = true) ||
                        habit.description?.contains(searchQuery, ignoreCase = true) == true
            }
        } else {
            when (currentFilter) {
                HabitFilterType.TODAY -> habitsData.todayHabits
                HabitFilterType.ALL -> habitsData.allUserHabits
                HabitFilterType.ARCHIVED -> habitsData.allUserHabits.filter { it.isArchived }
                HabitFilterType.COMPLETED -> habitsData.todayHabits.filter { it.isCompletedToday }
                HabitFilterType.PENDING -> habitsData.todayHabits.filter { !it.isCompletedToday && !it.isSkippedToday }
            }
        }

        val filteredHabits = if (currentFilter == HabitFilterType.ARCHIVED) {
            searchFiltered
        } else {
            searchFiltered.filter { !it.isArchived }
        }

        HabitListUiState(
            todayHabits = habitsData.todayHabits.filter { !it.isArchived },
            filteredHabits = filteredHabits,
            isLoading = isLoading,
            error = error,
            currentFilter = currentFilter,
            searchQuery = searchQuery,
            isSearchActive = isSearchActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState()
    )

    init {
        viewModelScope.launch {
            userPreferences.userIdFlow.firstOrNull()?.let { userId ->
                val yesterday = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(
                    kotlinx.datetime.DatePeriod(days = 1)
                )
                try {
                    markMissedHabitsUseCase(userId, yesterday)
                } catch (e: Exception) {
                    _error.value = context.getString(R.string.error_processing_missed_habits, e.message ?: "")
                }
            }
        }
    }

    private fun getCurrentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun triggerRefresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun setFilter(filter: HabitFilterType) {
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

    @SuppressLint("StringFormatInvalid")
    fun incrementHabitProgress(habitId: Long, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                updateHabitProgressValueUseCase(habitId, quantity)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_updating_habit_progress, e.message ?: "")
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun decrementHabitProgress(habitId: Long, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                updateHabitProgressValueUseCase(habitId, -quantity)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_updating_habit_progress, e.message ?: "")
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun markHabitAsSkipped(habitId: Long) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                markHabitAsSkippedUseCase(habitId)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_marking_habit_skipped, e.message ?: "")
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    fun toggleHabitArchived(habitId: Long, archive: Boolean) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                toggleHabitArchivedUseCase(habitId, archive)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_archiving_habit, e.message ?: "")
            }
        }
    }

    fun markHabitAsNotCompleted(habitId: Long) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                markHabitAsNotCompletedUseCase(habitId)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_undoing_skip, e.message ?: "")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}