package com.alejandro.habitjourney.features.dashboard.domain.usecase

import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.task.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Use Case que calcula el score de productividad de manera dinámica y correcta.
 *
 * LÓGICA DE CÁLCULO:
 * - Si solo hay hábitos: 100% basado en hábitos
 * - Si solo hay tareas: 100% basado en tareas
 * - Si hay 1 tarea: 30% tareas + 70% hábitos
 * - Si hay 2+ tareas: 40% tareas + 60% hábitos
 * - Sin contenido programado: 0% (válido)
 *
 * CORRECCIÓN CLAVE:
 * - Cuenta tareas PROGRAMADAS para hoy (no solo activas)
 * - Cuenta tareas COMPLETADAS hoy (aunque ya no estén activas)
 * - Adaptación dinámica de porcentajes según contenido disponible
 */
class CalculateProductivityScoreUseCase @Inject constructor() {

    /**
     * Calcula el score de productividad basado en hábitos y tareas del día actual.
     *
     * @param todayHabits Lista de hábitos programados para hoy con sus logs
     * @param todayTasks Lista de TODAS las tareas programadas para hoy (activas + completadas)
     * @return Score de productividad de 0 a 100
     */
    operator fun invoke(
        todayHabits: List<HabitWithLogs>,
        todayTasks: List<Task>
    ): ProductivityResult {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // === MÉTRICAS DE HÁBITOS ===
        val totalHabitsToday = todayHabits.size
        val completedHabitsToday = todayHabits.count { it.isCompletedToday }
        val habitCompletionRate = if (totalHabitsToday > 0) {
            completedHabitsToday.toFloat() / totalHabitsToday
        } else 0f

        // === MÉTRICAS DE TAREAS ===
        // Filtrar tareas que están programadas para hoy o vencen hoy
        val tasksForToday = todayTasks.filter { task ->
            task.dueDate == today ||
                    (task.dueDate == null && task.createdAt <= System.currentTimeMillis()) ||
                    (task.completionDate == today) // Incluir las completadas hoy
        }

        val totalTasksToday = tasksForToday.size
        val completedTasksToday = tasksForToday.count { it.isCompleted }
        val taskCompletionRate = if (totalTasksToday > 0) {
            completedTasksToday.toFloat() / totalTasksToday
        } else 0f

        // === CÁLCULO DINÁMICO DE PESOS ===
        val (habitWeight, taskWeight) = calculateDynamicWeights(
            hasHabits = totalHabitsToday > 0,
            hasTask = totalTasksToday > 0,
            taskCount = totalTasksToday
        )

        // === CÁLCULO FINAL ===
        val habitScore = habitCompletionRate * habitWeight
        val taskScore = taskCompletionRate * taskWeight
        val finalScore = ((habitScore + taskScore) * 100).toInt().coerceIn(0, 100)

        return ProductivityResult(
            score = finalScore,
            habitWeight = habitWeight,
            taskWeight = taskWeight,
            totalHabitsToday = totalHabitsToday,
            completedHabitsToday = completedHabitsToday,
            totalTasksToday = totalTasksToday,
            completedTasksToday = completedTasksToday,
            habitCompletionRate = habitCompletionRate,
            taskCompletionRate = taskCompletionRate
        )
    }

    /**
     * Calcula los pesos dinámicos según el contenido disponible.
     */
    private fun calculateDynamicWeights(
        hasHabits: Boolean,
        hasTask: Boolean,
        taskCount: Int
    ): Pair<Float, Float> {
        return when {
            // Solo hábitos
            hasHabits && !hasTask -> Pair(1.0f, 0.0f)

            // Solo tareas
            !hasHabits && hasTask -> Pair(0.0f, 1.0f)

            // Ambos: peso según número de tareas
            hasHabits && hasTask -> {
                when (taskCount) {
                    1 -> Pair(0.7f, 0.3f)  // 1 tarea: 30% tareas, 70% hábitos
                    else -> Pair(0.6f, 0.4f) // 2+ tareas: 40% tareas, 60% hábitos
                }
            }

            // Sin contenido
            else -> Pair(0.0f, 0.0f)
        }
    }
}

/**
 * Resultado del cálculo de productividad con todos los detalles.
 */
data class ProductivityResult(
    val score: Int,
    val habitWeight: Float,
    val taskWeight: Float,
    val totalHabitsToday: Int,
    val completedHabitsToday: Int,
    val totalTasksToday: Int,
    val completedTasksToday: Int,
    val habitCompletionRate: Float,
    val taskCompletionRate: Float
) {
    /**
     * Descripción de cómo se calculó el score para mostrar al usuario.
     */
    val calculationBreakdown: String
        get() {
            return when {
                habitWeight == 1.0f -> "100% basado en hábitos ($completedHabitsToday/$totalHabitsToday completados)"
                taskWeight == 1.0f -> "100% basado en tareas ($completedTasksToday/$totalTasksToday completadas)"
                else -> {
                    val habitPercentage = (habitWeight * 100).toInt()
                    val taskPercentage = (taskWeight * 100).toInt()
                    "$habitPercentage% hábitos ($completedHabitsToday/$totalHabitsToday) + $taskPercentage% tareas ($completedTasksToday/$totalTasksToday)"
                }
            }
        }
}