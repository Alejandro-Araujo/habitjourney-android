package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate


/**
 * Representa el estado de la UI para la pantalla de creación y edición de hábitos.
 *
 * Contiene todos los campos del formulario, así como los estados de carga,
 * guardado y error necesarios para gestionar la interfaz de usuario.
 *
 * @property habitId El ID del hábito. Es 0 si se está creando un nuevo hábito.
 * @property name El nombre del hábito.
 * @property description La descripción opcional del hábito.
 * @property type El [HabitType] del hábito (ej: DO, QUANTITATIVE).
 * @property frequency La regla de frecuencia seleccionada (ej: "daily", "weekly").
 * @property frequencyDays La lista de [Weekday] seleccionados si la frecuencia es semanal.
 * @property dailyTarget El objetivo numérico para hábitos contables.
 * @property startDate La fecha de inicio opcional para el hábito.
 * @property endDate La fecha de finalización opcional para el hábito.
 * @property isEditing `true` si la pantalla está en modo de edición, `false` si está en modo de creación.
 * @property isLoading `true` si se están cargando los datos de un hábito existente.
 * @property isSaving `true` si una operación de guardado está en progreso.
 * @property isSaved `true` si el hábito se ha guardado con éxito, para mostrar un diálogo de confirmación.
 * @property error Un mensaje de error para mostrar al usuario, o `null` si no hay error.
 * @property isArchived `true` si el hábito está archivado.
 * @property createdAt El timestamp de creación del hábito.
 */
data class CreateEditHabitUiState(
    val habitId: Long = 0L,
    val name: String = "",
    val description: String = "",
    val type: HabitType = HabitType.DO,
    val frequency: String = "daily",
    val frequencyDays: List<Weekday> = emptyList(),
    val dailyTarget: Int? = 1,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = 0L
) {
    /**
     * Propiedad computada que determina si el estado actual del formulario es válido para ser guardado.
     *
     * Reglas de validación:
     * - El nombre no puede estar vacío.
     * - El objetivo diario debe ser un número mayor que 0.
     * - Si la frecuencia es "semanal", la lista de días no puede estar vacía.
     *
     * @return `true` si los datos del formulario son válidos, `false` en caso contrario.
     */
    val isValid: Boolean
        get() = name.isNotBlank() &&
                (dailyTarget != null && dailyTarget > 0) &&
                (frequency != "weekly" || frequencyDays.isNotEmpty())
}