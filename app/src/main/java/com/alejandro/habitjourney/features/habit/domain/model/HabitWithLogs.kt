package com.alejandro.habitjourney.features.habit.domain.model

import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Clase contenedora que asocia un [Habit] con su lista de registros ([HabitLog]).
 *
 * Proporciona propiedades computadas para acceder fácilmente a datos derivados
 * como el estado de completitud de hoy, el progreso actual y la racha,
 * optimizando el rendimiento mediante cálculos perezosos (lazy).
 *
 * @property habit El modelo de dominio del hábito.
 * @property logs La lista de todos los registros de seguimiento asociados a este hábito.
 */
data class HabitWithLogs(
    val habit: Habit,
    val logs: List<HabitLog>
) {
    /**
     * El log específico para el día de hoy.
     * Se calcula de forma perezosa (una sola vez por instancia) para mayor eficiencia.
     * Es transient para que los serializadores (como Gson/Moshi) lo ignoren.
     * @return El [HabitLog] de hoy, o null si no existe.
     */
    @delegate:Transient
    val todayLog: HabitLog? by lazy {
        val todayDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        logs.find { it.date == todayDate }
    }

    /**
     * Progreso numérico actual del hábito para hoy (ej: contador de veces, minutos).
     * Devuelve 0 si no hay registro o si el valor es nulo.
     * @return El progreso de hoy como [Int].
     */
    @delegate:Transient
    val todayProgress: Int by lazy {
        todayLog?.value?.toInt() ?: 0
    }

    /**
     * Indica si el hábito se considera completado para el día de hoy.
     * Para hábitos contables, verifica si el progreso alcanza el objetivo.
     * Para hábitos de SÍ/NO, verifica si el estado es [LogStatus.COMPLETED].
     * @return `true` si el hábito está completado hoy, `false` en caso contrario.
     */
    @delegate:Transient
    val isCompletedToday: Boolean by lazy {
        val log = todayLog ?: return@lazy false

        return@lazy when {
            // Si no hay objetivo, se considera completado si el estado es COMPLETED.
            habit.dailyTarget == null || habit.dailyTarget <= 0 -> log.status == LogStatus.COMPLETED
            // Si hay objetivo, se comprueba si el valor lo alcanza o supera.
            else -> log.value != null && log.value >= habit.dailyTarget
        }
    }

    /**
     * Porcentaje de completitud del hábito para el día de hoy (de 0 a 100).
     * @return Un [Float] que representa el porcentaje.
     */
    @delegate:Transient
    val completionPercentageToday: Float by lazy {
        if (habit.dailyTarget != null && habit.dailyTarget > 0) {
            val progress = (todayProgress.toFloat() / habit.dailyTarget) * 100f
            return@lazy progress.coerceIn(0f, 100f)
        } else {
            return@lazy if (isCompletedToday) 100f else 0f
        }
    }

    /**
     * La racha actual de días consecutivos en los que el hábito se ha completado.
     * @return El número de días seguidos como [Int].
     */
    val currentStreak: Int
        get() = calculateCurrentStreak()

    /**
     * Calcula la racha actual basándose en los registros.
     * Un registro saltado o no completado rompe la racha.
     */
    private fun calculateCurrentStreak(): Int {
        if (logs.isEmpty()) return 0
        val sortedLogs = logs.sortedByDescending { it.date }

        var streak = 0
        var expectedDate: LocalDate? = null

        for (log in sortedLogs) {
            // Para el primer log completado que encontramos, inicializamos la racha.
            if (expectedDate == null) {
                if (log.status == LogStatus.COMPLETED) {
                    streak = 1
                    expectedDate = log.date.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
                }
                continue // Pasamos al siguiente log
            }

            // Si el log actual es del día esperado y está completado, continuamos la racha.
            if (log.date == expectedDate && log.status == LogStatus.COMPLETED) {
                streak++
                expectedDate = expectedDate.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
            } else if (log.date < expectedDate) {
                // Si encontramos un hueco en las fechas, la racha se ha roto antes.
                break
            }
            // Si log.date == expectedDate pero no está completado, se rompe la racha (handled by the next loop iteration).
        }
        return streak
    }
}
