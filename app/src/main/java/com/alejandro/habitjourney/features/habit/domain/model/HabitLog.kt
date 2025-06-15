package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import kotlinx.datetime.LocalDate

/**
 * Representa el modelo de dominio para un registro de seguimiento de un hábito.
 *
 * Esta clase encapsula el estado de un hábito en una fecha concreta.
 *
 * @property id El identificador único del registro (suele ser 0 para registros nuevos).
 * @property habitId El ID del hábito al que pertenece este registro.
 * @property date La fecha específica para la que se registra este progreso.
 * @property status El estado de este registro (ej: COMPLETADO, SALTADO), según [LogStatus].
 * @property value El valor de un hábito.
 * @property createdAt Timestamp de la creación del registro.
 */
data class HabitLog(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val status: LogStatus,
    val value: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
