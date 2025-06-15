package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitWithLogsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsNotCompletedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsSkippedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.ToggleHabitArchivedUseCase
import com.alejandro.habitjourney.features.habit.presentation.state.HabitDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalle de un hábito.
 *
 * Gestiona el estado de la UI ([HabitDetailUiState]) para un hábito específico,
 * carga sus datos y su historial de registros, y maneja las acciones del usuario
 * como archivar, omitir o deshacer una acción.
 *
 * @property getHabitWithLogsUseCase Caso de uso para obtener un hábito con todos sus registros.
 * @property toggleHabitArchivedUseCase Caso de uso para archivar o desarchivar un hábito.
 * @property markHabitAsSkippedUseCase Caso de uso para marcar un hábito como omitido.
 * @property markHabitAsNotCompletedUseCase Caso de uso para deshacer una acción de completado u omisión.
 */
@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitWithLogsUseCase: GetHabitWithLogsUseCase,
    private val toggleHabitArchivedUseCase: ToggleHabitArchivedUseCase,
    private val markHabitAsSkippedUseCase: MarkHabitAsSkippedUseCase,
    private val markHabitAsNotCompletedUseCase: MarkHabitAsNotCompletedUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    /**
     * Carga los detalles del hábito y sus registros, y actualiza el estado de la UI.
     * @param habitId El ID del hábito a cargar.
     */
    fun loadHabitDetail(habitId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getHabitWithLogsUseCase(habitId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: resourceProvider.getString(R.string.error_loading_habit_details)
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

    /** Calcula el progreso de hoy como un valor entre 0.0 y 1.0. */
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

    /** Calcula el progreso general como un valor entre 0.0 y 1.0. */
    private fun calculateOverallProgress(habitWithLogs: HabitWithLogs): Float {
        val totalLogs = habitWithLogs.logs.size
        val completedLogs = habitWithLogs.logs.count { it.status == LogStatus.COMPLETED }
        return if (totalLogs > 0) completedLogs.toFloat() / totalLogs else 0f
    }

    /** Devuelve un par que indica si el hábito está completado u omitido hoy. */
    private fun getTodayLogInfo(habitWithLogs: HabitWithLogs): Pair<Boolean, Boolean> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayLog = habitWithLogs.logs.find { it.date == today }

        val isCompletedToday = todayLog?.status == LogStatus.COMPLETED
        val isSkippedToday = todayLog?.status == LogStatus.SKIPPED

        return Pair(isCompletedToday, isSkippedToday)
    }

    /**
     * Cambia el estado de archivado del hábito actual.
     * Vuelve a cargar los detalles después de la operación.
     */
    fun archiveHabit() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                toggleHabitArchivedUseCase(currentHabit.id, !currentHabit.isArchived)
                // La recarga se activará automáticamente si el Flow del repositorio emite un nuevo valor.
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: resourceProvider.getString(R.string.error_archiving_habit)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Marca el hábito como omitido para el día de hoy.
     */
    fun markSkipped() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                markHabitAsSkippedUseCase(currentHabit.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: resourceProvider.getString(R.string.error_marking_habit_skipped)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /**
     * Deshace el estado de "omitido", marcando el hábito como no completado.
     */
    fun undoSkipped() {
        val currentHabit = _uiState.value.habitWithLogs?.habit ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isProcessing = true)
                markHabitAsNotCompletedUseCase(currentHabit.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: resourceProvider.getString(R.string.error_undoing_skip)
                )
            } finally {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    /** Comprueba si el hábito está completado hoy. */
    fun isCompletedToday(): Boolean {
        val habitWithLogs = _uiState.value.habitWithLogs ?: return false
        return getTodayLogInfo(habitWithLogs).first
    }

    /** Comprueba si el hábito está omitido hoy. */
    fun isSkippedToday(): Boolean {
        val habitWithLogs = _uiState.value.habitWithLogs ?: return false
        return getTodayLogInfo(habitWithLogs).second
    }

    /** Comprueba si se puede realizar la acción de omitir. */
    fun canToggleSkipped(): Boolean {
        val habit = _uiState.value.habitWithLogs?.habit ?: return false
        return !habit.isArchived
    }

    /** Limpia el mensaje de error del estado de la UI. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
