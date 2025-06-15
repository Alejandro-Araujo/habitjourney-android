package com.alejandro.habitjourney.features.dashboard.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardStats
import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * Use Case principal para obtener todos los datos consolidados del Dashboard.
 *
 * Responsabilidades:
 * - Recopilar datos de hábitos, tareas, notas y usuario
 * - Calcular estadísticas y métricas de productividad
 * - Generar score de productividad usando algoritmo corregido
 * - Calcular rachas de hábitos y días productivos
 * - Proporcionar datos listos para presentación en UI
 *
 * CORRECCIÓN IMPORTANTE:
 * - Ahora usa CalculateProductivityScoreUseCase para cálculo preciso
 * - Obtiene TODAS las tareas del día (no solo activas)
 * - Mapea correctamente tareas completadas vs programadas
 *
 * @param userPreferences Preferencias del usuario actual
 * @param userRepository Repositorio de usuarios
 * @param habitRepository Repositorio de hábitos
 * @param taskRepository Repositorio de tareas
 * @param noteRepository Repositorio de notas
 * @param calculateProductivityScoreUseCase Use case para cálculo correcto de productividad
 */
class GetDashboardDataUseCase @Inject constructor(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val calculateProductivityScoreUseCase: CalculateProductivityScoreUseCase
) {

    /**
     * Obtiene todos los datos del dashboard de manera reactiva.
     *
     * El flujo principal combina múltiples fuentes de datos:
     * 1. Datos básicos (usuario, hábitos, tareas activas)
     * 2. Tareas específicas del día (para cálculo correcto de productividad)
     * 3. Notas y estadísticas adicionales
     * 4. Cálculo de productividad con datos correctos
     *
     * @return Flow con Result<DashboardData> que contiene todos los datos consolidados
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<DashboardData>> {
        return userPreferences.getCurrentUserId().flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(Result.Error(Exception("No user logged in")))
            } else {
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val weekdayIndex = currentDate.dayOfWeek.ordinal

                // === FLUJO 1: DATOS BÁSICOS ===
                val basicDataFlow = combine(
                    userRepository.getLocalUser(),
                    habitRepository.getHabitsDueTodayWithCompletionCount(currentUserId, currentDate, weekdayIndex),
                    taskRepository.getActiveTasks(currentUserId), // Para mostrar en dashboard
                    taskRepository.getOverdueTasks(currentUserId, currentDate)
                ) { user, todayHabitsWithCount, activeTasks, overdueTasks ->
                    BasicDashboardData(user, todayHabitsWithCount, activeTasks, overdueTasks)
                }

                // === FLUJO 2: TAREAS DEL DÍA (PARA PRODUCTIVIDAD) ===
                val todayTasksFlow = taskRepository.getTasksForDate(currentUserId, currentDate)

                // === FLUJO 3: NOTAS Y ESTADÍSTICAS ===
                val notesAndStatsFlow = combine(
                    noteRepository.getActiveNotes(currentUserId),
                    flow { emit(calculateStreak(currentUserId, currentDate)) },
                    flow { emit(calculateProductiveDays(currentUserId, currentDate)) }
                ) { activeNotes, streak, productiveDays ->
                    NotesAndStats(activeNotes, streak, productiveDays)
                }

                // === COMBINACIÓN FINAL CON CÁLCULO DE PRODUCTIVIDAD ===
                combine(
                    basicDataFlow,
                    todayTasksFlow,
                    notesAndStatsFlow
                ) { basicData, allTodayTasks, notesAndStats ->

                    val user = basicData.user
                    val todayHabitsWithCount = basicData.todayHabitsWithCount
                    val activeTasks = basicData.activeTasks
                    val overdueTasks = basicData.overdueTasks
                    val activeNotes = notesAndStats.activeNotes
                    val streak = notesAndStats.streak
                    val productiveDays = notesAndStats.productiveDays

                    // === CONSTRUCCIÓN DE HABITWITHLOS ===
                    val todayHabitsWithLogs = todayHabitsWithCount.map { (habit, count) ->
                        val logs = habitRepository.getLogsForPeriod(
                            habitId = habit.id,
                            startDate = currentDate,
                            endDate = currentDate
                        ).first()
                        HabitWithLogs(habit, logs)
                    }

                    // === CÁLCULO CORRECTO DE PRODUCTIVIDAD ===
                    val productivityResult = calculateProductivityScoreUseCase(
                        todayHabits = todayHabitsWithLogs,
                        todayTasks = allTodayTasks // ✅ TODAS las tareas del día
                    )

                    // === DATOS PARA MOSTRAR EN DASHBOARD ===
                    val pendingTasks = activeTasks.filter { !it.isCompleted }
                        .sortedBy { it.dueDate }
                        .take(5)

                    val recentNotes = activeNotes
                        .sortedByDescending { it.updatedAt }
                        .take(3)

                    // === MÉTRICAS SEMANALES Y MENSUALES ===
                    val weeklyCompletionRate = calculateWeeklyHabitCompletionRate(currentUserId, currentDate)

                    val notesThisWeekCount = activeNotes.count { note ->
                        val noteDate = Instant.fromEpochMilliseconds(note.createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val weekStart = currentDate.minus(currentDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        noteDate >= weekStart
                    }

                    val totalWordsInNotes = activeNotes.sumOf { it.wordCount }

                    // === ESTADÍSTICAS CONSOLIDADAS ===
                    val dashboardStats = DashboardStats(
                        // Hábitos
                        totalHabitsToday = productivityResult.totalHabitsToday,
                        completedHabitsToday = productivityResult.completedHabitsToday,
                        currentStreak = streak,
                        longestStreak = 0, // Simplificado en MVP
                        weeklyHabitCompletionRate = weeklyCompletionRate,

                        // Tareas (CORREGIDO)
                        totalActiveTasks = activeTasks.size, // Para mostrar en dashboard
                        completedTasksToday = productivityResult.completedTasksToday, // ✅ Del día real
                        overdueTasks = overdueTasks.size,

                        // Notas
                        totalActiveNotes = activeNotes.size,
                        totalWords = totalWordsInNotes,
                        notesCreatedThisWeek = notesThisWeekCount,
                        productiveDaysThisMonth = productiveDays
                    )

                    // === RESULTADO FINAL ===
                    DashboardData(
                        user = user,
                        todayHabits = todayHabitsWithLogs,
                        pendingTasks = pendingTasks,
                        recentNotes = recentNotes,
                        dashboardStats = dashboardStats,
                        productivityResult = productivityResult
                    )
                }
                    .map<DashboardData, Result<DashboardData>> { data -> Result.Success(data) }
                    .onStart { emit(Result.Loading) }
                    .catch { e -> emit(Result.Error(e as? Exception ?: Exception(e))) }
            }
        }
    }

    /**
     * Calcula la racha actual de días consecutivos con hábitos completados.
     *
     * Algoritmo:
     * 1. Comienza desde hoy y retrocede día a día
     * 2. Si hoy no está completo, racha = 0
     * 3. Si un día anterior no está completo, para el conteo
     * 4. Si no hay hábitos un día, no rompe la racha pero tampoco la continúa
     *
     * @param userId ID del usuario
     * @param currentDate Fecha actual como referencia
     * @return Número de días consecutivos con hábitos completados
     */
    private suspend fun calculateStreak(userId: Long, currentDate: LocalDate): Int {
        var currentStreak = 0
        var checkDate = currentDate

        while (true) {
            val dayCompleted = checkIfDayCompleted(userId, checkDate)

            if (dayCompleted) {
                currentStreak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                // Si hoy no está completo, racha = 0
                if (checkDate == currentDate) {
                    return 0
                }
                // Si un día anterior no se cumplió, para el conteo
                break
            }
        }
        return currentStreak
    }

    /**
     * Verifica si un día específico se considera "completado" para la racha.
     *
     * Criterios:
     * - Debe haber hábitos programados para ese día
     * - TODOS los hábitos deben estar completados según su objetivo
     * - Para hábitos sin objetivo: valor > 0
     * - Para hábitos con objetivo: valor >= objetivo
     *
     * @param userId ID del usuario
     * @param date Fecha a verificar
     * @return true si el día está completado, false en caso contrario
     */
    private suspend fun checkIfDayCompleted(userId: Long, date: LocalDate): Boolean {
        val weekdayIndex = date.dayOfWeek.ordinal

        // Obtener hábitos que deberían ejecutarse ese día
        val habitsForDay = habitRepository.getActiveHabitsForUser(userId).first().filter { habit ->
            when (habit.frequency) {
                "DAILY" -> true
                "WEEKLY" -> habit.frequencyDays?.contains(Weekday.entries[weekdayIndex]) ?: false
                else -> false
            }
        }

        // Si no hay hábitos, el día no cuenta para la racha
        if (habitsForDay.isEmpty()) {
            return false
        }

        // Obtener completaciones reales de ese día
        val completions = habitRepository.getHabitsDueTodayWithCompletionCount(userId, date, weekdayIndex).first()

        // Verificar que TODOS los hábitos estén completados
        return habitsForDay.all { habitToCheck ->
            val completionData = completions.find { (habit, _) -> habit.id == habitToCheck.id }

            if (completionData != null) {
                val (habit, count) = completionData
                when {
                    habit.dailyTarget == null -> count > 0
                    else -> count >= habit.dailyTarget
                }
            } else {
                false // Hábito no completado
            }
        }
    }

    /**
     * Calcula la tasa de completitud semanal de hábitos.
     *
     * Considera:
     * - Solo días hasta hoy (no días futuros)
     * - Hábitos programados vs completados cada día
     * - Porcentaje ponderado por días con hábitos
     *
     * @param userId ID del usuario
     * @param currentDate Fecha actual como referencia
     * @return Porcentaje de completitud semanal (0-100%)
     */
    private suspend fun calculateWeeklyHabitCompletionRate(userId: Long, currentDate: LocalDate): Float {
        val weekStart = currentDate.minus(currentDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)

        var totalExpected = 0
        var totalCompleted = 0

        var checkDate = weekStart
        while (checkDate <= minOf(weekEnd, currentDate)) {
            val weekdayIndex = checkDate.dayOfWeek.ordinal
            val habitsWithCount = habitRepository.getHabitsDueTodayWithCompletionCount(userId, checkDate, weekdayIndex).first()

            totalExpected += habitsWithCount.size
            totalCompleted += habitsWithCount.count { (habit, count) ->
                when {
                    habit.dailyTarget == null -> count > 0
                    else -> count >= habit.dailyTarget
                }
            }
            checkDate = checkDate.plus(1, DateTimeUnit.DAY)
        }

        return if (totalExpected > 0) {
            (totalCompleted.toFloat() / totalExpected) * 100f
        } else {
            100f // Sin hábitos = 100% técnicamente
        }
    }

    /**
     * Calcula el número de días "productivos" en el mes actual.
     *
     * Un día se considera productivo si:
     * - Tiene hábitos programados
     * - Al menos 70% de los hábitos están completados
     *
     * @param userId ID del usuario
     * @param currentDate Fecha actual como referencia
     * @return Número de días productivos en el mes
     */
    private suspend fun calculateProductiveDays(userId: Long, currentDate: LocalDate): Int {
        val monthStart = LocalDate(currentDate.year, currentDate.month, 1)
        var productiveDays = 0
        var checkDate = monthStart

        while (checkDate <= currentDate) {
            val weekdayIndex = checkDate.dayOfWeek.ordinal
            val habitsWithCount = habitRepository.getHabitsDueTodayWithCompletionCount(userId, checkDate, weekdayIndex).first()

            if (habitsWithCount.isNotEmpty()) {
                val completionRate = habitsWithCount.count { (habit, count) ->
                    when {
                        habit.dailyTarget == null -> count > 0
                        else -> count >= habit.dailyTarget
                    }
                }.toFloat() / habitsWithCount.size

                if (completionRate >= 0.7f) {
                    productiveDays++
                }
            }
            checkDate = checkDate.plus(1, DateTimeUnit.DAY)
        }
        return productiveDays
    }
}

/**
 * Datos básicos del dashboard (usuario, hábitos y tareas activas).
 *
 * @param user Usuario actual
 * @param todayHabitsWithCount Hábitos de hoy con conteo de completaciones
 * @param activeTasks Tareas activas para mostrar en dashboard
 * @param overdueTasks Tareas vencidas
 */
data class BasicDashboardData(
    val user: com.alejandro.habitjourney.features.user.domain.model.User?,
    val todayHabitsWithCount: List<Pair<Habit, Int>>,
    val activeTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>,
    val overdueTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>
)

/**
 * Notas y estadísticas adicionales del dashboard.
 *
 * @param activeNotes Notas activas del usuario
 * @param streak Racha actual de días con hábitos completados
 * @param productiveDays Días productivos en el mes actual
 */
data class NotesAndStats(
    val activeNotes: List<com.alejandro.habitjourney.features.note.domain.model.Note>,
    val streak: Int,
    val productiveDays: Int
)