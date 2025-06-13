package com.alejandro.habitjourney.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R // Asegúrate de tener este import
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import com.alejandro.habitjourney.features.dashboard.presentation.state.DashboardUiState
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.usecase.LogHabitCompletionUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    // CAMBIO 1: Inyectamos tu ResourceProvider en lugar de Application
    private val resourceProvider: ResourceProvider,
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val logHabitCompletionUseCase: LogHabitCompletionUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            getDashboardDataUseCase().collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> {
                            currentState.copy(isLoading = true, error = null)
                        }
                        is Result.Success -> {
                            val dashboardData = result.data

                            // CAMBIO 2: Generamos los mensajes dinámicos aquí
                            val (summary, quote) = generateUiMessages(dashboardData)

                            val isEmpty = dashboardData.todayHabits.isEmpty() &&
                                    dashboardData.pendingTasks.isEmpty() &&
                                    dashboardData.recentNotes.isEmpty() &&
                                    (dashboardData.user == null || dashboardData.user.name.equals("Usuario", ignoreCase = true) || dashboardData.user.name.isEmpty())

                            // CAMBIO 3: Pasamos los mensajes al copy() del state
                            currentState.copy(
                                isLoading = false,
                                error = null,
                                user = dashboardData.user,
                                todayHabits = dashboardData.todayHabits,
                                totalHabitsToday = dashboardData.dashboardStats.totalHabitsToday,
                                completedHabitsToday = dashboardData.dashboardStats.completedHabitsToday,
                                currentStreak = dashboardData.dashboardStats.currentStreak,
                                longestStreak = dashboardData.dashboardStats.longestStreak,
                                activeTasks = dashboardData.pendingTasks,
                                totalActiveTasks = dashboardData.dashboardStats.totalActiveTasks,
                                overdueTasks = dashboardData.dashboardStats.overdueTasks,
                                recentNotes = dashboardData.recentNotes,
                                totalNotes = dashboardData.dashboardStats.totalActiveNotes,
                                totalWords = dashboardData.dashboardStats.totalWords,
                                weeklyCompletionRate = dashboardData.dashboardStats.weeklyHabitCompletionRate,
                                productiveDaysThisMonth = dashboardData.dashboardStats.productiveDaysThisMonth,
                                isEmpty = isEmpty,
                                summaryMessage = summary,      // <-- NUEVO
                                motivationalQuote = quote        // <-- NUEVO
                            )
                        }
                        is Result.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.exception.message ?: result.message ?: "Error desconocido al cargar el dashboard"
                            )
                        }
                    }
                }
                _isRefreshing.value = false
            }
        }
    }

    // CAMBIO 4: Nueva función privada para generar los mensajes
    private fun generateUiMessages(data: DashboardData): Pair<String, String> {
        val stats = data.dashboardStats
        val stateForCalc = DashboardUiState( // Creamos un estado temporal para usar los getters de cálculo
            isEmpty = data.todayHabits.isEmpty() && data.pendingTasks.isEmpty() && data.recentNotes.isEmpty(),
            completedHabitsToday = stats.completedHabitsToday,
            totalHabitsToday = stats.totalHabitsToday,
            overdueTasks = stats.overdueTasks,
            currentStreak = stats.currentStreak
        )

        val summary = when {
            stateForCalc.isEmpty -> resourceProvider.getString(R.string.dashboard_summary_start)
            stateForCalc.completedHabitsToday == stateForCalc.totalHabitsToday && !stateForCalc.hasOverdueTasks -> {
                if (stateForCalc.currentStreak > 1) {
                    resourceProvider.getString(R.string.dashboard_summary_all_habits_completed_with_streak, stateForCalc.currentStreak)
                } else {
                    resourceProvider.getString(R.string.dashboard_summary_all_habits_completed_no_streak)
                }
            }
            stateForCalc.habitCompletionPercentage >= 0.8f && !stateForCalc.hasOverdueTasks -> {
                val percentage = (stateForCalc.habitCompletionPercentage * 100).toInt()
                resourceProvider.getString(R.string.dashboard_summary_good_progress, percentage)
            }
            stateForCalc.hasOverdueTasks -> {
                resourceProvider.getQuantityString(R.plurals.dashboard_summary_overdue_tasks, stateForCalc.overdueTasks, stateForCalc.overdueTasks)
            }
            stateForCalc.habitCompletionPercentage >= 0.5f -> resourceProvider.getString(R.string.dashboard_summary_decent_progress)
            else -> resourceProvider.getString(R.string.dashboard_summary_keep_going)
        }

        val quote = when (stateForCalc.productivityScore) {
            in 90..100 -> resourceProvider.getString(R.string.dashboard_quote_excellent)
            in 70..89 -> resourceProvider.getString(R.string.dashboard_quote_great)
            in 50..69 -> resourceProvider.getString(R.string.dashboard_quote_good)
            else -> resourceProvider.getString(R.string.dashboard_quote_default)
        }

        return Pair(summary, quote)
    }

    val greetingMessage: String
        get() {
            val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
            val greetingResId = when (hour) {
                in 5..11 -> R.string.greeting_morning
                in 12..17 -> R.string.greeting_afternoon
                else -> R.string.greeting_evening
            }
            return resourceProvider.getString(greetingResId)
        }

    fun toggleHabitCompletion(habitId: Long, habitWithLogs: HabitWithLogs) {
        viewModelScope.launch {
            try {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                val todayLog = habitWithLogs.logs.find { it.date == today }
                val currentValue = todayLog?.value ?: 0f
                val newValue = currentValue + 1f
                logHabitCompletionUseCase(
                    habitId = habitId,
                    date = today,
                    value = newValue
                )
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Error al actualizar el hábito: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                toggleTaskCompletionUseCase(taskId, !currentStatus)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = "Error al actualizar la tarea: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadDashboardData()
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }
}