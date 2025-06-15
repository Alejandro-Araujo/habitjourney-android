package com.alejandro.habitjourney.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
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

/**
 * ViewModel principal para la pantalla de Dashboard.
 *
 * Responsabilidades:
 * - Cargar y mostrar datos consolidados del usuario
 * - Generar mensajes motivacionales y resúmenes dinámicos
 * - Manejar interacciones rápidas con hábitos y tareas
 * - Gestionar estados de carga, refresh y errores
 * - Calcular métricas de productividad y progreso
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val logHabitCompletionUseCase: LogHabitCompletionUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Carga todos los datos necesarios para el dashboard desde el dominio.
     */
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
                            val (summary, quote) = generateUiMessages(dashboardData)
                            val isEmpty = isDashboardEmpty(dashboardData)

                            currentState.copy(
                                isLoading = false,
                                error = null,
                                user = dashboardData.user,
                                // Hábitos
                                todayHabits = dashboardData.todayHabits,
                                totalHabitsToday = dashboardData.dashboardStats.totalHabitsToday,
                                completedHabitsToday = dashboardData.dashboardStats.completedHabitsToday,
                                currentStreak = dashboardData.dashboardStats.currentStreak,
                                longestStreak = dashboardData.dashboardStats.longestStreak,
                                weeklyCompletionRate = dashboardData.dashboardStats.weeklyHabitCompletionRate,
                                // Tareas
                                activeTasks = dashboardData.pendingTasks,
                                totalActiveTasks = dashboardData.dashboardStats.totalActiveTasks,
                                completedTasksToday = dashboardData.dashboardStats.completedTasksToday,
                                overdueTasks = dashboardData.dashboardStats.overdueTasks,
                                // Notas
                                recentNotes = dashboardData.recentNotes,
                                totalNotes = dashboardData.dashboardStats.totalActiveNotes,
                                totalWords = dashboardData.dashboardStats.totalWords,
                                // Métricas
                                productiveDaysThisMonth = dashboardData.dashboardStats.productiveDaysThisMonth,
                                productivityResult = dashboardData.productivityResult,
                                // Estado
                                isEmpty = isEmpty,
                                summaryMessage = summary,
                                motivationalQuote = quote
                            )
                        }
                        is Result.Error -> {
                            val errorMessage = result.exception.let { exception ->
                                resourceProvider.getString(
                                    R.string.error_loading_dashboard_with_details,
                                    errorHandler.getErrorMessage(exception)
                                )
                            }

                            currentState.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                }
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Determina si el dashboard está vacío (usuario nuevo sin datos).
     */
    private fun isDashboardEmpty(data: DashboardData): Boolean {
        return data.todayHabits.isEmpty() &&
                data.pendingTasks.isEmpty() &&
                data.recentNotes.isEmpty() &&
                (data.user == null || data.user.name.equals("Usuario", ignoreCase = true) || data.user.name.isEmpty())
    }

    /**
     * Genera mensajes dinámicos basados en el estado actual del usuario.
     *
     * @param data Datos consolidados del dashboard
     * @return Par de mensajes: (resumen del día, frase motivacional)
     */
    private fun generateUiMessages(data: DashboardData): Pair<String, String> {
        val stats = data.dashboardStats
        val productivityResult = data.productivityResult

        val stateForCalc = DashboardUiState(
            isEmpty = isDashboardEmpty(data),
            completedHabitsToday = stats.completedHabitsToday,
            totalHabitsToday = stats.totalHabitsToday,
            overdueTasks = stats.overdueTasks,
            currentStreak = stats.currentStreak,
            productivityResult = productivityResult
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

    /**
     * Genera mensaje de saludo dinámico basado en la hora del día.
     */
    val greetingMessage: String
        get() {
            val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
            val greetingResId = when (hour) {
                in 6..12 -> R.string.greeting_morning
                in 13..19 -> R.string.greeting_afternoon
                else -> R.string.greeting_evening
            }
            return resourceProvider.getString(greetingResId)
        }

    /**
     * Alterna el estado de completado de un hábito desde el dashboard.
     *
     * @param habitId ID del hábito a actualizar
     * @param habitWithLogs Datos del hábito con sus logs
     */
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
                        error = resourceProvider.getString(
                            R.string.error_updating_habit_dashboard,
                            errorHandler.getErrorMessage(e)
                        )
                    )
                }
            }
        }
    }

    /**
     * Alterna el estado de completado de una tarea desde el dashboard.
     *
     * @param taskId ID de la tarea a actualizar
     * @param currentStatus Estado actual de completado
     */
    fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                toggleTaskCompletionUseCase(taskId, !currentStatus)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        error = resourceProvider.getString(
                            R.string.error_updating_task_dashboard,
                            errorHandler.getErrorMessage(e)
                        )
                    )
                }
            }
        }
    }

    /**
     * Fuerza una recarga completa de los datos del dashboard.
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadDashboardData()
        }
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }
}