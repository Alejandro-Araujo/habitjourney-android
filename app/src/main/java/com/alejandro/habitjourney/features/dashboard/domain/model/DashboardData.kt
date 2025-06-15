package com.alejandro.habitjourney.features.dashboard.domain.model

import com.alejandro.habitjourney.features.dashboard.domain.usecase.ProductivityResult
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.task.domain.model.Task
import com.alejandro.habitjourney.features.user.domain.model.User


/**
 * Datos consolidados del dashboard que incluyen toda la información necesaria
 * para mostrar el estado actual del usuario.
 *
 * Contiene:
 * - Información del usuario actual
 * - Hábitos programados para hoy con sus logs
 * - Tareas pendientes relevantes para mostrar
 * - Notas recientes del usuario
 * - Estadísticas consolidadas
 * - Resultado del cálculo de productividad (NUEVO)
 *
 * @param user Usuario actual logueado
 * @param todayHabits Lista de hábitos programados para hoy con sus logs
 * @param pendingTasks Tareas pendientes filtradas para mostrar en dashboard (máximo 5)
 * @param recentNotes Notas recientes del usuario (máximo 3)
 * @param dashboardStats Estadísticas consolidadas y métricas
 * @param productivityResult Resultado completo del cálculo de productividad
 */
data class DashboardData(
    val user: User?,
    val todayHabits: List<HabitWithLogs>,
    val pendingTasks: List<Task>,
    val recentNotes: List<Note>,
    val dashboardStats: DashboardStats,
    val productivityResult: ProductivityResult
)