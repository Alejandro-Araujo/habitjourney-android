package com.alejandro.habitjourney.features.dashboard.domain.usecase

import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.data.local.result.Result
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardData
import com.alejandro.habitjourney.features.dashboard.domain.model.DashboardStats
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

class GetDashboardDataUseCase @Inject constructor(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Result<DashboardData>> {
        return userPreferences.getCurrentUserId().flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(Result.Error(Exception("No user logged in")))
            } else {
                val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val weekdayIndex = currentDate.dayOfWeek.ordinal

                val basicDataFlow = combine(
                    userRepository.getLocalUser(),
                    habitRepository.getHabitsDueTodayWithCompletionCount(currentUserId, currentDate, weekdayIndex),
                    taskRepository.getActiveTasks(currentUserId),
                    taskRepository.getCompletedTasksToday(currentUserId, currentDate),
                    taskRepository.getOverdueTasks(currentUserId, currentDate)
                ) { user, todayHabitsWithCount, activeTasks, completedTasksToday, overdueTasks ->
                    BasicDashboardData(user, todayHabitsWithCount, activeTasks, completedTasksToday, overdueTasks)
                }

                val notesAndStatsFlow = combine(
                    noteRepository.getActiveNotes(currentUserId),
                    flow { emit(calculateStreaks(currentUserId, currentDate)) },
                    flow { emit(calculateProductiveDays(currentUserId, currentDate)) }
                ) { activeNotes, streaks, productiveDays ->
                    NotesAndStats(activeNotes, streaks, productiveDays)
                }

                // Este es el flujo principal que construye DashboardData
                val dashboardDataFlow: Flow<DashboardData> = combine(basicDataFlow, notesAndStatsFlow) { basicData, notesAndStats ->

                    val user = basicData.user
                    val todayHabitsWithCount = basicData.todayHabitsWithCount
                    val activeTasks = basicData.activeTasks
                    val completedTasksToday = basicData.completedTasksToday
                    val overdueTasks = basicData.overdueTasks
                    val activeNotes = notesAndStats.activeNotes
                    val streaks = notesAndStats.streaks
                    val productiveDays = notesAndStats.productiveDays

                    val todayHabitsWithLogs = todayHabitsWithCount.map { (habit, count) ->
                        val logs = habitRepository.getLogsForPeriod(
                            habitId = habit.id,
                            startDate = currentDate,
                            endDate = currentDate
                        ).first()
                        HabitWithLogs(habit, logs)
                    }

                    val pendingTasks = activeTasks.filter { !it.isCompleted }.sortedBy { it.dueDate }.take(5)
                    val recentNotes = activeNotes.sortedByDescending { it.updatedAt }.take(3)

                    val completedHabitsTodayCount = todayHabitsWithCount.count { (habit, count) ->
                        when {
                            habit.dailyTarget == null -> count > 0
                            else -> count >= habit.dailyTarget
                        }
                    }

                    // Estas funciones de cálculo son suspend y usan .first() internamente
                    val weeklyCompletionRate = calculateWeeklyHabitCompletionRate(currentUserId, currentDate)

                    val notesThisWeekCount = activeNotes.count { note ->
                        val noteDate = Instant.fromEpochMilliseconds(note.createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val weekStart = currentDate.minus(currentDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
                        noteDate >= weekStart
                    }
                    val totalWordsInNotes = activeNotes.sumOf { it.wordCount }

                    val dashboardStats = DashboardStats(
                        totalHabitsToday = todayHabitsWithCount.size,
                        completedHabitsToday = completedHabitsTodayCount,
                        currentStreak = streaks.first,
                        longestStreak = streaks.second,
                        weeklyHabitCompletionRate = weeklyCompletionRate,
                        totalActiveTasks = activeTasks.size,
                        completedTasksToday = completedTasksToday.size,
                        overdueTasks = overdueTasks.size,
                        totalActiveNotes = activeNotes.size,
                        totalWords = totalWordsInNotes,
                        notesCreatedThisWeek = notesThisWeekCount,
                        productiveDaysThisMonth = productiveDays
                    )

                    DashboardData(user, todayHabitsWithLogs, pendingTasks, recentNotes, dashboardStats)
                    // --- FIN DE LA LÓGICA DE TRANSFORMACIÓN ---
                }

                // Envuelve el dashboardDataFlow con los estados de Result
                dashboardDataFlow
                    .map<DashboardData, Result<DashboardData>> { data -> Result.Success(data) } // Transforma el éxito
                    .onStart { emit(Result.Loading) } // Emitir Loading al principio de este flujo
                    .catch { e -> emit(Result.Error(e as? Exception ?: Exception(e))) } // Capturar excepciones del dashboardDataFlow
            }
        }
    }

    private suspend fun calculateStreaks(userId: Long, currentDate: LocalDate): Pair<Int, Int> {
        // Obtener todos los hábitos activos del usuario
        val habits = habitRepository.getActiveHabitsForUser(userId).first()

        if (habits.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var checkDate = currentDate

        // Calcular racha actual (hacia atrás desde hoy)
        while (true) {
            val dayCompleted = checkIfDayCompleted(userId, checkDate, habits)

            if (dayCompleted) {
                currentStreak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }

            // Límite de seguridad
            if (currentStreak > 365) break
        }

        // Calcular racha más larga (últimos 90 días)
        val startDate = currentDate.minus(90, DateTimeUnit.DAY)
        checkDate = startDate

        while (checkDate <= currentDate) {
            val dayCompleted = checkIfDayCompleted(userId, checkDate, habits)

            if (dayCompleted) {
                tempStreak++
                longestStreak = maxOf(longestStreak, tempStreak)
            } else {
                tempStreak = 0
            }

            checkDate = checkDate.plus(1, DateTimeUnit.DAY)
        }

        return Pair(currentStreak, longestStreak)
    }

    private suspend fun checkIfDayCompleted(userId: Long, date: LocalDate, habits: List<com.alejandro.habitjourney.features.habit.domain.model.Habit>): Boolean {
        val weekdayIndex = date.dayOfWeek.ordinal

        // Obtener hábitos que deberían completarse ese día
        val habitsForDay = habits.filter { habit ->
            when (habit.frequency) {
                "DAILY" -> true
                "WEEKLY" -> habit.frequencyDays?.contains(
                    Weekday.entries[weekdayIndex]
                ) ?: false
                else -> false
            }
        }

        if (habitsForDay.isEmpty()) return true // Si no hay hábitos ese día, cuenta como completado

        // Verificar si todos los hábitos del día fueron completados
        val completions = habitRepository.getHabitsDueTodayWithCompletionCount(userId, date, weekdayIndex).first()

        return completions.all { (habit, count) ->
            when {
                habit.dailyTarget == null -> count > 0
                else -> count >= habit.dailyTarget
            }
        }
    }

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
            100f
        }
    }

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

                // Considerar un día productivo si se completó al menos el 70% de los hábitos
                if (completionRate >= 0.7f) {
                    productiveDays++
                }
            }

            checkDate = checkDate.plus(1, DateTimeUnit.DAY)
        }

        return productiveDays
    }
}