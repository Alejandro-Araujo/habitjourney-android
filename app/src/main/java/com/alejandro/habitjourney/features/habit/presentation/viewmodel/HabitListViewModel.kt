package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.habit.domain.usecase.GetAllUserHabitsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitsDueTodayWithCompletionCountUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetLogForDateUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsNotCompletedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsSkippedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkMissedHabitsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.ToggleHabitArchivedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitProgressValueUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitUseCase
import com.alejandro.habitjourney.features.habit.presentation.ui.HabitIconMapper
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
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
    private val _showTodayOnlyFilter = MutableStateFlow(true)

    // CLAVE: Creamos un trigger para forzar refrescos manuales cuando sea necesario
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

            // CORRECCIÓN: Combinamos los flows reactivos y obtenemos los logs de forma reactiva
            combine(
                getAllUserHabitsUseCase(userId),
                getHabitsDueTodayWithCompletionCountUseCase(userId, today, weekdayIndex)
            ) { allUserHabits, todayHabitsWithCounts ->
                _isLoading.value = false

                // Para cada hábito, necesitamos obtener su log actual de forma reactiva
                val habitsWithReactiveLogs = allUserHabits.map { habit ->
                    // Obtener el log para hoy de este hábito de forma reactiva
                    getLogForDateUseCase(habit.id, today).map { todayLog ->
                        // Buscar el conteo de completado si este hábito también es de hoy
                        val todayCompletionCount = todayHabitsWithCounts
                            .find { it.first.id == habit.id }?.second ?: 0

                        habit.toHabitListItemUiModel(
                            currentCompletionCount = todayCompletionCount,
                            todayLog = todayLog,
                            icon = HabitIconMapper.getIconForHabitType(habit.type)
                        )
                    }
                }

                // Combinar todos los flows de hábitos individuales
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

    // Estado de la UI que se consume en la interfaz de usuario.
    val uiState: StateFlow<HabitListUiState> = combine(
        habitsDataFlow,
        _isLoading,
        _error,
        _showTodayOnlyFilter
    ) { habitsData, isLoading, error, showTodayOnly ->

        // Filtrar solo hábitos activos (no archivados)
        val activeHabits = habitsData.allUserHabits.filter { !it.isArchived }
        val activeTodayHabits = habitsData.todayHabits.filter { !it.isArchived }

        HabitListUiState(
            todayHabits = activeTodayHabits,
            filteredHabits = if (showTodayOnly) activeTodayHabits else activeHabits,
            isLoading = isLoading,
            error = error,
            showTodayOnly = showTodayOnly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitListUiState()
    )

    // Lógica para procesar hábitos "missed" al inicio.
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

    /**
     * Fuerza un refresh manual del estado.
     * Útil después de operaciones que pueden no triggear automáticamente el flow.
     */
    private fun triggerRefresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    /**
     * Alterna el filtro para mostrar solo los hábitos del día actual o todos los hábitos activos.
     */
    fun toggleShowTodayOnly() {
        _showTodayOnlyFilter.value = !_showTodayOnlyFilter.value
    }

    /**
     * Incrementa el progreso de un hábito (botón de completar).
     * @param habitId El ID del hábito a actualizar.
     * @param quantity El valor a añadir al progreso (por defecto 1f).
     */
    @SuppressLint("StringFormatInvalid")
    fun incrementHabitProgress(habitId: Long, quantity: Float = 1f) {
        viewModelScope.launch {
            try {
                userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                updateHabitProgressValueUseCase(habitId, quantity)
                // Los flows reactivos deberían actualizarse automáticamente
                // pero si hay problemas, podemos forzar un refresh
                triggerRefresh()
            } catch (e: Exception) {
                _error.value = context.getString(R.string.error_updating_habit_progress, e.message ?: "")
            }
        }
    }

    /**
     * Decrementa el progreso de un hábito (botón de deshacer).
     * @param habitId El ID del hábito a actualizar.
     * @param quantity El valor a restar al progreso (por defecto 1f).
     */
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

    /**
     * Marca un hábito como omitido (skipped) para el día actual.
     * @param habitId El ID del hábito a marcar.
     */
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

    /**
     * Alterna el estado de archivado de un hábito.
     * @param habitId El ID del hábito.
     * @param archive True para archivar, false para desarchivar.
     */
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

    /**
     * Limpia cualquier mensaje de error visible en la UI.
     */
    fun clearError() {
        _error.value = null
    }
}

private data class HabitsData(
    val allUserHabits: List<HabitListItemUiModel>, // Ya convertidos a UI models
    val todayHabits: List<HabitListItemUiModel>    // Ya convertidos a UI models
)

data class HabitListUiState(
    val todayHabits: List<HabitListItemUiModel> = emptyList(),
    val filteredHabits: List<HabitListItemUiModel> = emptyList(),
    val showTodayOnly: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)