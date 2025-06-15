package com.alejandro.habitjourney.features.habit.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.usecase.CreateHabitUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitByIdUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitUseCase
import com.alejandro.habitjourney.features.habit.presentation.state.CreateEditHabitUiState
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject


/**
 * ViewModel para la pantalla de creación y edición de hábitos.
 *
 * Gestiona el estado de la UI ([CreateEditHabitUiState]), maneja la lógica del formulario,
 * valida las entradas del usuario y se comunica con los casos de uso para
 * crear o actualizar un hábito en la base de datos.
 *
 * @property createHabitUseCase Caso de uso para crear un nuevo hábito.
 * @property updateHabitUseCase Caso de uso para actualizar un hábito existente.
 * @property getHabitByIdUseCase Caso de uso para obtener los datos de un hábito por su ID.
 * @property userPreferences Preferencias para obtener el ID del usuario actual.
 */
@HiltViewModel
class CreateEditHabitViewModel @Inject constructor(
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val userPreferences: UserPreferences,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditHabitUiState())
    val uiState: StateFlow<CreateEditHabitUiState> = _uiState.asStateFlow()

    /** Actualiza el nombre del hábito en el estado de la UI. */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    /** Actualiza la descripción del hábito en el estado de la UI. */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /** Actualiza la frecuencia. Si no es semanal, limpia los días de la semana seleccionados. */
    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
        if (frequency != "weekly") {
            _uiState.value = _uiState.value.copy(frequencyDays = emptyList())
        }
    }

    /** Actualiza la lista de días de la semana seleccionados. */
    fun updateFrequencyDays(days: List<Weekday>) {
        _uiState.value = _uiState.value.copy(frequencyDays = days)
    }

    /** Actualiza el objetivo diario. */
    fun updateDailyTarget(target: Int?) {
        _uiState.value = _uiState.value.copy(dailyTarget = target)
    }

    /** Actualiza la fecha de inicio. */
    fun updateStartDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    /** Actualiza la fecha de fin. */
    fun updateEndDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(endDate = date)
    }

    /**
     * Carga los datos de un hábito existente por su ID y actualiza el estado de la UI
     * para entrar en modo de edición.
     * @param habitId El ID del hábito a cargar.
     */
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
                    error = e.message ?: resourceProvider.getString(R.string.error_loading_habit)
                )
            }
        }
    }

    /**
     * Guarda el hábito actual. Realiza validaciones y decide si crear o actualizar.
     * @param onSuccess Callback que se ejecuta si el guardado es exitoso, usualmente para navegar hacia atrás.
     */
    fun saveHabit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, error = null) }

                // Validación del nombre
                if (_uiState.value.name.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = resourceProvider.getString(R.string.error_habit_name_empty)
                        )
                    }
                    return@launch
                }

                if (_uiState.value.dailyTarget == null || _uiState.value.dailyTarget!! <= 0) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = resourceProvider.getString(R.string.error_daily_target_required)
                        )
                    }
                    return@launch
                }

                if (_uiState.value.frequency == "weekly" && _uiState.value.frequencyDays.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = resourceProvider.getString(R.string.error_frequency_days_required)
                        )
                    }
                    return@launch
                }

                val userId = userPreferences.getUserId()
                    ?: throw IllegalStateException(resourceProvider.getString(R.string.error_user_not_logged_in))

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
                onSuccess() // se llama desde la UI al cerrar el diálogo de éxito

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: resourceProvider.getString(R.string.error_saving_habit)
                    )
                }
            }
        }
    }

    /**
     * Prepara el estado de la UI con los datos de un hábito para su edición.
     * @param habit El hábito a editar.
     */
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

    /** Limpia el mensaje de error del estado de la UI. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /** Resetea el estado de guardado, usualmente después de que el diálogo de éxito es cerrado. */
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}