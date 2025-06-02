package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn


data class HabitWithLogs(
    val habit: Habit,
    val logs: List<HabitLog> // Esta lista puede ser completa o parcial según el contexto
) {

    // --- Propiedades específicas para "HOY" (optimizadas con by lazy) ---
    // Estas son las que usarás principalmente para los items en DashboardData.todayHabits

    /**
     * El log específico para el día de hoy, calculado una sola vez por instancia.
     * Es transient para que los serializadores (como Gson/Moshi) lo ignoren si esta clase se serializa.
     */
    @delegate:Transient
    val todayLog: HabitLog? by lazy {
        // Obtenemos la fecha actual una sola vez para las propiedades lazy dependientes.
        val todayDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        logs.find { it.date == todayDate }
    }

    /**
     * Progreso actual del hábito para el día de hoy (ej. contador de veces, minutos).
     * Calculado una sola vez por instancia.
     */
    @delegate:Transient
    val todayProgress: Int by lazy { // O Float si necesitas decimales
        todayLog?.value?.toInt() ?: 0
    }

    /**
     * Indica si el hábito se considera completado para el día de hoy.
     * Calculado una sola vez por instancia.
     */
    @delegate:Transient
    val isCompletedToday: Boolean by lazy {
        val log = todayLog ?: return@lazy false // Si no hay log de hoy, no está completado

        return@lazy when {
            // Para hábitos de sí/no o sin objetivo numérico específico,
            // consideramos completado si el estado es COMPLETED.
            habit.dailyTarget == null || habit.dailyTarget <= 0 -> log.status == LogStatus.COMPLETED
            // Para hábitos con objetivo numérico.
            else -> log.value!! >= habit.dailyTarget
        }
    }

    /**
     * Porcentaje de completitud del hábito para el día de hoy.
     * Calculado una sola vez por instancia.
     */
    @delegate:Transient
    val completionPercentageToday: Float by lazy {
        return@lazy when {
            habit.dailyTarget == null || habit.dailyTarget <= 0 -> if (isCompletedToday) 100f else 0f
            habit.dailyTarget > 0 -> {
                val progress = (todayProgress.toFloat() / habit.dailyTarget) * 100f
                progress.coerceIn(0f, 100f) // Asegura que esté entre 0 y 100
            }
            else -> 0f
        }
    }

    // --- Funciones generales (operan sobre la lista 'logs' proporcionada) ---
    // Estas funciones pueden permanecer, pero recuerda que en el contexto de DashboardData.todayHabits,
    // 'logs' solo contendrá los registros de hoy, por lo que estas funciones darán resultados
    // basados solo en esa información (ej. racha de 0 o 1 si se completó hoy).

    val currentStreak: Int // Tu implementación actual
        get() = calculateCurrentStreak()

    val bestStreak: Int // Tu implementación actual
        get() = calculateBestStreak()

    val completionRate: Float // Tu implementación actual
        get() = calculateCompletionRate()

    private fun calculateCurrentStreak(): Int {
        // Tu lógica actual: opera sobre la lista 'logs' disponible en esta instancia.
        // Si 'logs' solo tiene los de hoy, esta racha será 0 o 1.
        if (logs.isEmpty()) return 0
        val sortedLogs = logs.sortedByDescending { it.date }
        var streak = 0
        // Asumiendo que para la racha "actual" del hábito, te refieres a días consecutivos terminando en el log más reciente.
        // Si los logs son solo de hoy, y está completado, la racha es 1.
        // Esta lógica es más para una racha histórica del hábito si 'logs' es completo.
        var currentDateExpected = sortedLogs.firstOrNull()?.date ?: return 0
        for (log in sortedLogs) {
            if (log.date == currentDateExpected && log.status == LogStatus.COMPLETED) {
                streak++
                currentDateExpected = currentDateExpected.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
            } else if (log.date < currentDateExpected) { // Se saltó un día o más
                break
            } else if (log.date == currentDateExpected && log.status != LogStatus.COMPLETED) { // Día esperado pero no completado
                break
            }
            // Si hay múltiples logs para el mismo día, esta lógica simple podría necesitar ajuste
            // si el orden dentro del mismo día importa o si solo debe contar una vez por día.
        }
        return streak
    }

    private fun calculateBestStreak(): Int {
        // Tu lógica actual: opera sobre la lista 'logs' disponible en esta instancia.
        if (logs.isEmpty()) return 0
        val sortedLogsByDate = logs
            .filter { it.status == LogStatus.COMPLETED } // Solo considerar logs completados
            .distinctBy { it.date } // Asegurar un log por día para el cálculo de racha
            .sortedBy { it.date }

        if (sortedLogsByDate.isEmpty()) return 0

        var maxStreak = 0
        var currentStreak = 0
        var expectedDate = sortedLogsByDate.first().date.minus(1, kotlinx.datetime.DateTimeUnit.DAY) // Para empezar la primera comparación

        for (log in sortedLogsByDate) {
            if (log.date == expectedDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)) {
                currentStreak++
            } else {
                // Se rompió la racha (o es el primer elemento después de una ruptura)
                currentStreak = 1 // Inicia nueva racha
            }
            maxStreak = maxOf(maxStreak, currentStreak)
            expectedDate = log.date
        }
        return maxStreak
    }

    private fun calculateCompletionRate(): Float {
        // Tu lógica actual: opera sobre la lista 'logs' disponible en esta instancia.
        if (logs.isEmpty()) return 0f
        val completedCount = logs.count { it.status == LogStatus.COMPLETED }
        // Aquí, 'logs.size' podría ser 1 si solo tienes el log de hoy.
        // Si quieres una tasa de completitud histórica, 'logs' debe ser la lista completa.
        // Si es para hoy: ¿(completado hoy ? 100 : 0) / (hábitos esperados hoy=1)?
        return (completedCount.toFloat() / logs.size) * 100f
    }
}