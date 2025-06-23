package com.alejandro.habitjourney.features.dashboard.presentation.state

import com.alejandro.habitjourney.features.dashboard.domain.usecase.ProductivityResult
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.user.domain.model.User

/**
 * Estado UI del Dashboard que centraliza toda la información necesaria para la pantalla principal.
 *
 * Responsabilidades:
 * - Mantener datos consolidados del usuario (hábitos, tareas, notas)
 * - Proporcionar métricas calculadas de productividad
 * - Gestionar estados de carga y error
 * - Generar mensajes UI dinámicos
 *
 * NOTA IMPORTANTE: El cálculo de productividad ahora se maneja externamente
 * mediante CalculateProductivityScoreUseCase para mayor precisión.
 */
data class DashboardUiState(
    // === ESTADOS GENERALES ===
    val isLoading: Boolean = true,
    val error: String? = null,
    val user: User? = null,
    val isEmpty: Boolean = false,

    // === HÁBITOS ===
    /** Lista de hábitos programados para hoy con sus logs correspondientes */
    val todayHabits: List<HabitWithLogs> = emptyList(),

    /** Total de hábitos programados para el día actual */
    val totalHabitsToday: Int = 0,

    /** Número de hábitos completados en el día actual */
    val completedHabitsToday: Int = 0,

    /** Racha actual de días consecutivos con hábitos completados */
    val currentStreak: Int = 0,

    /** Racha más larga alcanzada por el usuario */
    val longestStreak: Int = 0,

    /** Tasa de completitud semanal de hábitos (0.0 a 1.0) */
    val weeklyCompletionRate: Float = 0f,

    // === TAREAS ===
    /** Lista de tareas activas/pendientes para mostrar en el dashboard */
    val activeTasks: List<Task> = emptyList(),

    /** Total de tareas activas sin completar */
    val totalActiveTasks: Int = 0,

    /** Número de tareas completadas en el día actual */
    val completedTasksToday: Int = 0,

    /** Número de tareas vencidas sin completar */
    val overdueTasks: Int = 0,

    // === NOTAS ===
    /** Lista de notas recientes para mostrar en el dashboard */
    val recentNotes: List<Note> = emptyList(),

    /** Total de notas activas del usuario */
    val totalNotes: Int = 0,

    /** Suma total de palabras en todas las notas */
    val totalWords: Int = 0,

    // === MÉTRICAS DE PRODUCTIVIDAD ===
    /** Resultado completo del cálculo de productividad */
    val productivityResult: ProductivityResult? = null,

    /** Días productivos en el mes actual */
    val productiveDaysThisMonth: Int = 0,

) {

    // === PROPIEDADES CALCULADAS PARA COMPATIBILIDAD ===

    /**
     * Porcentaje de hábitos completados hoy (0.0 a 1.0).
     * Calculado dinámicamente para mantener compatibilidad.
     */
    val habitCompletionPercentage: Float
        get() = if (totalHabitsToday > 0) {
            completedHabitsToday.toFloat() / totalHabitsToday
        } else 0f

    /**
     * Indica si hay tareas vencidas pendientes.
     */
    val hasOverdueTasks: Boolean
        get() = overdueTasks > 0

    /**
     * Score de productividad calculado por el UseCase especializado.
     * Valor entre 0 y 100.
     */
    val productivityScore: Int
        get() = productivityResult?.score ?: 0

    /**
     * Porcentaje de tareas completadas hoy (0.0 a 1.0).
     * NOTA: Esto ahora se basa en tareas programadas para hoy, no solo activas.
     */
    val taskCompletionPercentage: Float
        get() = productivityResult?.taskCompletionRate ?: 0f

    /**
     * Indica si el usuario está teniendo un día productivo (>= 70% score).
     */
    val isProductiveDay: Boolean
        get() = productivityScore >= 70

    /**
     * Nivel de productividad para determinar colores e iconos en la UI.
     */
    val productivityLevel: ProductivityLevel
        get() = when (productivityScore) {
            in 90..100 -> ProductivityLevel.EXCELLENT
            in 70..89 -> ProductivityLevel.GOOD
            in 50..69 -> ProductivityLevel.AVERAGE
            in 25..49 -> ProductivityLevel.LOW
            else -> ProductivityLevel.VERY_LOW
        }

    /**
     * Descripción detallada de cómo se calculó el score para mostrar al usuario.
     * Útil para el diálogo de información de estadísticas.
     */
    val calculationBreakdown: String
        get() = productivityResult?.calculationBreakdown ?: "Sin datos suficientes para calcular"
}

/**
 * Niveles de productividad para determinar la presentación visual en la UI.
 *
 * Cada nivel tiene asociados colores, iconos y animaciones específicas:
 * - EXCELLENT: Verde brillante, efectos de celebración
 * - GOOD: Azul, indicadores positivos
 * - AVERAGE: Lila, neutro
 * - LOW: Naranja/Rojo claro, motivacional
 */
enum class ProductivityLevel {
    /** 90-100%: Rendimiento excelente */
    EXCELLENT,

    /** 70-89%: Buen rendimiento */
    GOOD,

    /** 50-69%: Rendimiento promedio */
    AVERAGE,

    /** 25-49%: Rendimiento bajo */
    LOW,

    /** 0-24%: Rendimiento muy bajo */
    VERY_LOW
}