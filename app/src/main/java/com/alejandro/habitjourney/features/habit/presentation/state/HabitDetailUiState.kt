package com.alejandro.habitjourney.features.habit.presentation.state

import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs

/**
 * Representa el estado de la UI para la pantalla de detalle de un hábito.
 *
 * Contiene los datos del hábito y sus registros, así como estados derivados y
 * banderas para gestionar la carga y los errores en la interfaz de usuario.
 *
 * @property habitWithLogs El objeto que contiene el hábito y su lista completa de registros. Es nulo durante la carga inicial.
 * @property todayProgress El progreso de hoy como un valor flotante (0.0 a 1.0), para barras de progreso.
 * @property overallProgress El progreso histórico general como un valor flotante (0.0 a 1.0).
 * @property isLoading `true` si se están cargando los datos iniciales del hábito.
 * @property isProcessing `true` si se está procesando una acción del usuario (ej: registrar un progreso).
 * @property error Un mensaje de error para mostrar al usuario, o `null` si no hay error.
 */
data class HabitDetailUiState(
    val habitWithLogs: HabitWithLogs? = null,
    val todayProgress: Float = 0f,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)