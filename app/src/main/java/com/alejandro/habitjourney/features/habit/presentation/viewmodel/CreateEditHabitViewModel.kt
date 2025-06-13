package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.usecase.CreateHabitUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitByIdUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitUseCase
import com.alejandro.habitjourney.features.habit.presentation.state.CreateEditHabitUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class CreateEditHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditHabitUiState())
    val uiState: StateFlow<CreateEditHabitUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateType(type: HabitType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
        // Limpiar días de frecuencia si no es weekly
        if (frequency != "weekly") {
            _uiState.value = _uiState.value.copy(frequencyDays = emptyList())
        }
    }

    fun updateFrequencyDays(days: List<Weekday>) {
        _uiState.value = _uiState.value.copy(frequencyDays = days)
    }

    fun updateDailyTarget(target: Int?) {
        _uiState.value = _uiState.value.copy(dailyTarget = target)
    }

    fun updateStartDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun updateEndDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }

    fun loadHabitById(habitId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val habit = getHabitByIdUseCase(habitId)
                if (habit != null) {
                    loadHabitForEdit(habit)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: context.getString(R.string.error_loading_habit)
                )
            }
        }
    }

    fun saveHabit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, error = null) }

                // Validación del nombre
                if (_uiState.value.name.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = context.getString(R.string.error_habit_name_empty)
                        )
                    }
                    return@launch
                }

                // DailyTarget es siempre obligatorio y debe ser > 0
                if (_uiState.value.dailyTarget == null || _uiState.value.dailyTarget!! <= 0) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = context.getString(R.string.error_daily_target_required)
                        )
                    }
                    return@launch
                }

                // Validar días de frecuencia para weekly
                if (_uiState.value.frequency == "weekly" && _uiState.value.frequencyDays.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = context.getString(R.string.error_frequency_days_required)
                        )
                    }
                    return@launch
                }

                val userId = userPreferences.getUserId()
                    ?: throw IllegalStateException(context.getString(R.string.error_user_not_logged_in))

                val habit = Habit(
                    id = _uiState.value.habitId,
                    userId = userId,
                    name = _uiState.value.name.trim(),
                    description = _uiState.value.description.takeIf { it.isNotBlank() }?.trim(),
                    type = _uiState.value.type,
                    frequency = _uiState.value.frequency,
                    frequencyDays = _uiState.value.frequencyDays.takeIf { it.isNotEmpty() },
                    dailyTarget = _uiState.value.dailyTarget,
                    startDate = _uiState.value.startDate,
                    endDate = _uiState.value.endDate,
                    isArchived = _uiState.value.isArchived,
                    createdAt = if (_uiState.value.isEditing) _uiState.value.createdAt else Clock.System.now().toEpochMilliseconds()
                )

                if (_uiState.value.isEditing) {
                    updateHabitUseCase(habit)
                } else {
                    createHabitUseCase(habit)
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                onSuccess()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: context.getString(R.string.error_saving_habit)
                    )
                }
            }
        }
    }

    fun loadHabitForEdit(habit: Habit) {
        _uiState.value = CreateEditHabitUiState(
            habitId = habit.id,
            name = habit.name,
            description = habit.description ?: "",
            type = habit.type,
            frequency = habit.frequency,
            frequencyDays = habit.frequencyDays ?: emptyList(),
            dailyTarget = habit.dailyTarget,
            startDate = habit.startDate,
            endDate = habit.endDate,
            isEditing = true,
            isArchived = habit.isArchived,
            createdAt = habit.createdAt
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}