package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import kotlinx.datetime.LocalDate

/**
 * Representa el modelo de dominio de un hábito.
 *
 * Esta clase contiene todos los atributos y la configuración que definen un hábito
 * dentro de la lógica de negocio de la aplicación. Es la representación que se utiliza
 * en los Casos de Uso y ViewModels.
 *
 * @property id El identificador único del hábito.
 * @property userId El ID del usuario al que pertenece este hábito.
 * @property name El nombre del hábito (ej: "Leer 10 páginas").
 * @property description Una descripción opcional con más detalles sobre el hábito.
 * @property type El tipo de hábito, según el enum [HabitType].
 * @property frequency Describe la regla de frecuencia (actualmente no implementado, futuro).
 * @property frequencyDays Para hábitos semanales, la lista de [Weekday] en los que debe realizarse. Es nulo para otros tipos de frecuencia.
 * @property dailyTarget El objetivo diario para hábitos
 * @property startDate Fecha opcional de inicio del hábito.
 * @property endDate Fecha opcional de finalización del hábito.
 * @property isArchived `true` si el hábito está archivado y no debe aparecer en las listas activas.
 * @property createdAt Timestamp de la creación del hábito.
 */
data class Habit(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val type: HabitType,
    val frequency: String,
    val frequencyDays: List<Weekday>?,
    val dailyTarget: Int?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isArchived: Boolean,
    val createdAt: Long
)
