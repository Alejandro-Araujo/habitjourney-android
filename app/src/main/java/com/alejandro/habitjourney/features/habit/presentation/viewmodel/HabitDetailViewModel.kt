package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitWithLogsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsNotCompletedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsSkippedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.ToggleHabitArchivedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject


@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitWithLogsUseCase: GetHabitWithLogsUseCase,
    private val toggleHabitArchivedUseCase: ToggleHabitArchivedUseCase,
    private val markHabitAsSkippedUseCase: MarkHabitAsSkippedUseCase,
    private val markHabitAsNotCompletedUseCase: MarkHabitAsNotCompletedUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    fun loadHabitDetail(habitId: Long) {
        viewModelScope.launch {
            getHabitWithLogsUseCase(habitId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_loading_habit_details)
                    )
                }
                .collect { habitWithLogs ->
                    val todayProgress = calculateTodayProgress(habitWithLogs)
                    val overallProgress = calculateOverallProgress(habitWithLogs)

                    _uiState.value = _uiState.value.copy(
                        habitWithLogs = habitWithLogs,
                        todayProgress = todayProgress,
                        overallProgress = overallProgress,
                        isLoading = false
                    )
                }
        }
    }

    private fun calculateTodayProgress(habitWithLogs: HabitWithLogs): Float {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayLog = habitWithLogs.logs.find { it.date == today }
        val habit = habitWithLogs.habit

        return when {
            todayLog == null -> 0f
            habit.dailyTarget != null && habit.dailyTarget > 0 -> {
                // Para hábitos con objetivo numérico: valor actual / objetivo
                val currentValue = todayLog.value ?: 0f
                (currentValue / habit.dailyTarget).coerceIn(0f, 1f)
            }
            else -> {
                // Para hábitos booleanos: completado = 1.0, no completado = 0.0
                if (todayLog.status == LogStatus.COMPLETED) 1f else 0f
            }
        }
    }

    private fun calculateOverallProgress(habitWithLogs: HabitWithLogs): Float {
        val totalLogs = habitWithLogs.logs.size
        val completedLogs = habitWithLogs.logs.count { it.status == LogStatus.COMPLETED }
        return if (totalLogs > 0) completedLogs.toFloat() / totalLogs else 0f
    }

    // Obtener información del estado de hoy
    private fun getTodayLogInfo(habitWithLogs: HabitWithLogs): Pair<Boolean, Boolean> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayLog = habitWithLogs.logs.find { it.date == today }

        val isCompletedToday = todayLog?.status == LogStatus.COMPLETED
        val isSkippedToday = todayLog?.status == LogStatus.SKIPPED

        return Pair(isCompletedToday, isSkippedToday)
    }

    fun archiveHabit() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleHabitArchivedUseCase(currentHabit.id, !currentHabit.isArchived)
                loadHabitDetail(currentHabit.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: context.getString(R.string.error_archiving_habit),
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun markSkipped() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                markHabitAsSkippedUseCase(currentHabit.id)
                loadHabitDetail(currentHabit.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: context.getString(R.string.error_marking_habit_skipped),
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun undoSkipped() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                markHabitAsNotCompletedUseCase(currentHabit.id)
                loadHabitDetail(currentHabit.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: context.getString(R.string.error_undoing_skip),
                    isProcessing = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    // Funciones para obtener el estado actual
    fun isCompletedToday(): Boolean {
        val habitWithLogs = _uiState.value.habitWithLogs ?: return false
        return getTodayLogInfo(habitWithLogs).first
    }

    fun isSkippedToday(): Boolean {
        val habitWithLogs = _uiState.value.habitWithLogs ?: return false
        return getTodayLogInfo(habitWithLogs).second
    }

    fun canToggleSkipped(): Boolean {
        val habitWithLogs = _uiState.value.habitWithLogs ?: return false
        val habit = habitWithLogs.habit

        // Solo se puede saltar si el hábito no está archivado
        return !habit.isArchived
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HabitDetailUiState(
    val habitWithLogs: HabitWithLogs? = null,
    val todayProgress: Float = 0f,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)