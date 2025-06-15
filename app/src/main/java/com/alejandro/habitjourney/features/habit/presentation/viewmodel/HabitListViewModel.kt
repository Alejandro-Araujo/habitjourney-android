package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.habit.domain.usecase.*
import com.alejandro.habitjourney.features.habit.presentation.screen.HabitIconMapper
import com.alejandro.habitjourney.features.habit.presentation.state.HabitFilterType
import com.alejandro.habitjourney.features.habit.presentation.state.HabitListUiState
import com.alejandro.habitjourney.features.habit.presentation.state.HabitsData
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * ViewModel para la pantalla de lista de hábitos.
 *
 * Gestiona el estado de la UI ([HabitListUiState]), incluyendo la carga de datos,
 * el filtrado, la búsqueda y el manejo de todas las interacciones del usuario con la lista.
 * Utiliza un enfoque reactivo con Flows para mantener la UI actualizada automáticamente.
 *
 * @param getAllUserHabitsUseCase Caso de uso para obtener todos los hábitos del usuario.
 * @param getHabitsDueTodayWithCompletionCountUseCase Caso de uso para obtener los hábitos de hoy con su progreso.
 * @param updateHabitProgressValueUseCase Caso de uso para actualizar el progreso numérico de un hábito.
 * @param markHabitAsSkippedUseCase Caso de uso para marcar un hábito como omitido.
 * @param getLogForDateUseCase Caso de uso para obtener el registro de un hábito en una fecha concreta.
 * @param markHabitAsNotCompletedUseCase Caso de uso para marcar un hábito como no completado.
 * @param toggleHabitArchivedUseCase Caso de uso para archivar o desarchivar un hábito.
 * @param markMissedHabitsUseCase Caso de uso para procesar hábitos no completados del día anterior.
 * @param userPreferences Preferencias para obtener el ID del usuario actual.
 */
@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val getAllUserHabitsUseCase: GetAllUserHabitsUseCase,
    private val getHabitsDueTodayWithCompletionCountUseCase: GetHabitsDueTodayWithCompletionCountUseCase,
    private val updateHabitProgressValueUseCase: UpdateHabitProgressValueUseCase,
    private val markHabitAsSkippedUseCase: MarkHabitAsSkippedUseCase,
    private val getLogForDateUseCase: GetLogForDateUseCase,
    private val markHabitAsNotCompletedUseCase: MarkHabitAsNotCompletedUseCase,
    private val toggleHabitArchivedUseCase: ToggleHabitArchivedUseCase,
    private val markMissedHabitsUseCase: MarkMissedHabitsUseCase,
    private val userPreferences: UserPreferences,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    // --- Flujos de estado privados que controlan la UI ---
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _currentFilter = MutableStateFlow(HabitFilterType.TODAY)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    /** Un disparador para forzar la recarga de los datos. */
    private val _refreshTrigger = MutableStateFlow(0L)

    /**
     * Flujo reactivo que obtiene los datos maestros de los hábitos (todos y los de hoy)
     * cada vez que cambia el ID de usuario o se dispara una actualización.
     */
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

            // Combina los flujos de "todos los hábitos" y "hábitos de hoy"
            combine(
                getAllUserHabitsUseCase(userId),
                getHabitsDueTodayWithCompletionCountUseCase(userId, today, weekdayIndex)
            ) { allUserHabits, todayHabitsWithCounts ->
                _isLoading.value = false

                // Mapea cada hábito a un modelo de UI reactivo que observa su log de hoy
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

                // Combina los resultados de los modelos de UI reactivos en una lista final
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
                    _error.value = exception.message ?: resourceProvider.getString(R.string.error_unknown)
                    _isLoading.value = false
                    emit(HabitsData(emptyList(), emptyList()))
                }
        } else {
            _error.value = resourceProvider.getString(R.string.error_user_not_logged_in)
            _isLoading.value = false
            flowOf(HabitsData(emptyList(), emptyList()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsData(emptyList(), emptyList())
    )

    /**
     * El estado final de la UI que la vista observa.
     * Combina los datos de los hábitos con los estados de filtro, búsqueda y carga.
     */
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

    // Al iniciar el ViewModel, comprueba y marca los hábitos perdidos del día anterior.
    init {
        viewModelScope.launch {
            userPreferences.userIdFlow.firstOrNull()?.let { userId ->
                val yesterday = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(
                    kotlinx.datetime.DatePeriod(days = 1)
                )
                try {
                    markMissedHabitsUseCase(userId, yesterday)
                } catch (e: Exception) {
                    _error.value = resourceProvider.getString(R.string.error_processing_missed_habits, e.message ?: "")
                }
            }
        }
    }

    private fun getCurrentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun triggerRefresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    /** Establece el filtro actual para la lista de hábitos. */
    fun setFilter(filter: HabitFilterType) {
        _currentFilter.value = filter
    }

    /** Actualiza el término de búsqueda. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Activa o desactiva el modo de búsqueda. */
    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    /** Incrementa el progreso de un hábito. */
    fun incrementHabitProgress(habitId: Long, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

                updateHabitProgressValueUseCase(habitId, quantity)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_updating_habit_progress, e.message ?: "")
            }
        }
    }

    fun decrementHabitProgress(habitId: Long, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

                updateHabitProgressValueUseCase(habitId, -quantity)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_updating_habit_progress, e.message ?: "")
            }
        }
    }

    fun markHabitAsSkipped(habitId: Long) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

                markHabitAsSkippedUseCase(habitId)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_marking_habit_skipped, e.message ?: "")
            }
        }
    }

    fun toggleHabitArchived(habitId: Long, archive: Boolean) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

                toggleHabitArchivedUseCase(habitId, archive)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_archiving_habit, e.message ?: "")
            }
        }
    }

    fun markHabitAsNotCompleted(habitId: Long) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

                markHabitAsNotCompletedUseCase(habitId)
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = resourceProvider.getString(R.string.error_undoing_skip, e.message ?: "")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}