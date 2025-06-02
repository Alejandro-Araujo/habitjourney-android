package com.alejandro.habitjourney.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.core.data.local.result.Result

import com.alejandro.habitjourney.features.dashboard.domain.repository.DashboardRepository
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import com.alejandro.habitjourney.features.dashboard.presentation.state.DashboardUiState
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.usecase.LogHabitCompletionUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import com.alejandro.habitjourney.features.user.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
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
                        is Result.Loading -> { // No es necesario el path completo si importaste Result
                            currentState.copy(isLoading = true, error = null)
                        }
                        is Result.Success -> {
                            val dashboardData = result.data // dashboardData ahora tiene .user en lugar de .userName

                            // Determinar si está vacío basado en los datos recibidos
                            // Ahora usamos dashboardData.user en lugar de dashboardData.userName
                            val isEmpty = dashboardData.todayHabits.isEmpty() &&
                                    dashboardData.pendingTasks.isEmpty() &&
                                    dashboardData.recentNotes.isEmpty() &&
                                    (dashboardData.user == null || dashboardData.user.name == "Usuario" || dashboardData.user.name.isEmpty())

                            currentState.copy(
                                isLoading = false,
                                error = null,
                                user = dashboardData.user, // <--- CAMBIO PRINCIPAL AQUÍ: Asignación directa
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
                                isEmpty = isEmpty
                            )
                        }
                        is Result.Error -> { // No es necesario el path completo si importaste Result
                            currentState.copy(
                                isLoading = false,
                                error = result.exception.message ?: result.message ?: "Error desconocido al cargar el dashboard",
                                isEmpty = currentState.user == null && currentState.todayHabits.isEmpty() && currentState.activeTasks.isEmpty() && currentState.recentNotes.isEmpty()
                            )
                        }
                    }
                }
                _isRefreshing.value = false
            }
        }
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

    // Computed properties para la UI
    val greetingMessage: String
        get() {
            val hour = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .hour
            return when (hour) {
                in 5..11 -> "¡Buenos días"
                in 12..17 -> "¡Buenas tardes"
                else -> "¡Buenas noches"
            }
        }
}