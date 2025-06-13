package com.alejandro.habitjourney.features.dashboard.domain.usecase


import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import javax.inject.Inject
import kotlin.math.ceil

class CalculateStreakUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository
) {
    /**
     * Calcula la racha actual del usuario basada en hábitos (y opcionalmente tareas)
     *
     * Reglas:
     * - 1 hábito/tarea: necesita 100% completado
     * - 2 hábitos/tareas: necesita al menos 50% (1 de 2)
     * - 3+ hábitos/tareas: necesita al menos 66% (2 de 3, 3 de 4, etc.)
     *
     * @param userId ID del usuario
     * @param currentDate Fecha actual
     * @param includeTasks Si incluir tareas en el cálculo (default: false para MVP)
     * @return Racha actual en días
     */
    suspend fun invoke(
        userId: Long,
        currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        includeTasks: Boolean = false
    ): Int {
        var streak = 0
        var checkDate = currentDate

        // Verificar días hacia atrás hasta encontrar un día incompleto
        while (streak < 365) { // Límite razonable para evitar loops infinitos
            val dayCompleted = checkIfDayMeetsStreakCriteria(userId, checkDate, includeTasks)

            if (dayCompleted) {
                streak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                // Si el día actual (streak == 0) no está completado, la racha es 0
                // Si es un día anterior, mantenemos la racha acumulada
                break
            }
        }

        return streak
    }

    /**
     * Verifica si un día específico cumple los criterios para mantener la racha
     */
    private suspend fun checkIfDayMeetsStreakCriteria(
        userId: Long,
        date: LocalDate,
        includeTasks: Boolean
    ): Boolean {
        val weekdayIndex = date.dayOfWeek.ordinal

        // Obtener hábitos del día con sus completaciones
        val habitsWithCount = habitRepository.getHabitsDueTodayWithCompletionCount(
            userId,
            date,
            weekdayIndex
        ).first()

        var totalItems = habitsWithCount.size
        var completedItems = habitsWithCount.count { (habit, count) ->
            when {
                habit.dailyTarget == null -> count > 0
                else -> count >= habit.dailyTarget
            }
        }

        // Incluir tareas si está habilitado
        if (includeTasks) {
            val tasksForDay = taskRepository.getCompletedTasksToday(userId, date).first()
            totalItems += tasksForDay.size
            completedItems += tasksForDay.count { it.isCompleted }
        }

        // Si no hay nada que hacer ese día, cuenta como completado
        if (totalItems == 0) return true

        // Calcular el porcentaje mínimo requerido según la cantidad de items
        val requiredPercentage = when (totalItems) {
            1 -> 1.0f      // 100% (1 de 1)
            2 -> 0.5f      // 50% (1 de 2)
            else -> 0.66f  // 66% (2 de 3, 3 de 4, etc.)
        }

        val requiredCompleted = ceil(totalItems * requiredPercentage).toInt()

        return completedItems >= requiredCompleted
    }

    /**
     * Obtiene información detallada sobre el cálculo de la racha
     * Útil para mostrar en un dialog informativo
     */
    fun getStreakCalculationInfo(): StreakCalculationInfo {
        return StreakCalculationInfo(
            rules = listOf(
                StreakRule(1, 100, "Completa tu único hábito del día"),
                StreakRule(2, 50, "Completa al menos 1 de 2 hábitos"),
                StreakRule(3, 66, "Completa al menos 2 de 3 hábitos o más")
            ),
            description = "Tu racha aumenta cada día que completes el porcentaje mínimo requerido de tus hábitos. " +
                    "¡La consistencia es la clave del éxito!"
        )
    }
}

data class StreakCalculationInfo(
    val rules: List<StreakRule>,
    val description: String
)

data class StreakRule(
    val habitCount: Int,
    val requiredPercentage: Int,
    val explanation: String
)