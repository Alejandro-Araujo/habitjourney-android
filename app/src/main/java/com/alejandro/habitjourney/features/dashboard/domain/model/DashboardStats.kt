package com.alejandro.habitjourney.features.dashboard.domain.model


/**
 * Estadísticas consolidadas del dashboard para métricas y visualización.
 *
 * Incluye métricas de:
 * - Hábitos: completitud diaria, rachas, tasas semanales
 * - Tareas: activas, completadas hoy, vencidas
 * - Notas: totales, palabras, creadas esta semana
 * - Productividad: días productivos del mes
 *
 * @param totalHabitsToday Total de hábitos programados para hoy
 * @param completedHabitsToday Hábitos completados en el día actual
 * @param currentStreak Racha actual de días consecutivos con hábitos completados
 * @param longestStreak Racha más larga alcanzada (simplificado en MVP)
 * @param weeklyHabitCompletionRate Tasa de completitud semanal de hábitos (0-100%)
 * @param totalActiveTasks Total de tareas activas (para mostrar en dashboard)
 * @param completedTasksToday Tareas completadas en el día actual
 * @param overdueTasks Número de tareas vencidas
 * @param totalActiveNotes Total de notas activas del usuario
 * @param totalWords Suma de palabras en todas las notas
 * @param notesCreatedThisWeek Notas creadas en la semana actual
 * @param productiveDaysThisMonth Días productivos en el mes actual
 */
data class DashboardStats(
    // === HÁBITOS ===
    val totalHabitsToday: Int,
    val completedHabitsToday: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyHabitCompletionRate: Float,

    // === TAREAS ===
    val totalActiveTasks: Int,
    val completedTasksToday: Int,
    val overdueTasks: Int,

    // === NOTAS ===
    val totalActiveNotes: Int,
    val totalWords: Int,
    val notesCreatedThisWeek: Int,

    // === PRODUCTIVIDAD ===
    val productiveDaysThisMonth: Int
)