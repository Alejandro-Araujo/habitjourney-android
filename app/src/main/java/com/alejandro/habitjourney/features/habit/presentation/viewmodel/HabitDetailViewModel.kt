package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitWithLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitWithLogsUseCase: GetHabitWithLogsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    fun loadHabitDetail(habitId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                getHabitWithLogsUseCase(habitId).collect { habitWithLogs ->
                    val todayProgress = calculateTodayProgress(habitWithLogs)
                    val overallProgress = calculateOverallProgress(habitWithLogs)

                    _uiState.value = _uiState.value.copy(
                        habitWithLogs = habitWithLogs,
                        todayProgress = todayProgress,
                        overallProgress = overallProgress,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: context.getString(R.string.error_loading_habit_details)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HabitDetailUiState(
    val habitWithLogs: HabitWithLogs? = null,
    val todayProgress: Float = 0f,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)