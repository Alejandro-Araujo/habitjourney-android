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
                    flow { emit(calculateStreak(currentUserId, currentDate)) },
                    flow { emit(calculateProductiveDays(currentUserId, currentDate)) }
                ) { activeNotes, streak, productiveDays ->
                    NotesAndStats(activeNotes, streak, productiveDays)
                }

                combine(basicDataFlow, notesAndStatsFlow) { basicData, notesAndStats ->
                    val user = basicData.user
                    val todayHabitsWithCount = basicData.todayHabitsWithCount
                    val activeTasks = basicData.activeTasks
                    val completedTasksToday = basicData.completedTasksToday
                    val overdueTasks = basicData.overdueTasks
                    val activeNotes = notesAndStats.activeNotes
                    val streak = notesAndStats.streak
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
                        currentStreak = streak,
                        longestStreak = 0, // Simplificado, ya no se calcula
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
                }
                    .map<DashboardData, Result<DashboardData>> { data -> Result.Success(data) }
                    .onStart { emit(Result.Loading) }
                    .catch { e -> emit(Result.Error(e as? Exception ?: Exception(e))) }
            }
        }
    }

    private suspend fun calculateStreak(userId: Long, currentDate: LocalDate): Int {
        var currentStreak = 0
        var checkDate = currentDate

        while (true) {
            val dayCompleted = checkIfDayCompleted(userId, checkDate)

            // Si se completó el día, aumentamos la racha y retrocedemos un día.
            if (dayCompleted) {
                currentStreak++
                checkDate = checkDate.minus(1, DateTimeUnit.DAY)
            } else {
                // Si el día que estamos comprobando es hoy y no está completo, la racha es 0.
                if (checkDate == currentDate) {
                    return 0
                }
                // Si ya teníamos una racha y el día anterior a esa racha no se cumplió, paramos.
                break
            }
        }
        return currentStreak
    }

    private suspend fun checkIfDayCompleted(userId: Long, date: LocalDate): Boolean {
        val weekdayIndex = date.dayOfWeek.ordinal

        val habitsForDay = habitRepository.getActiveHabitsForUser(userId).first().filter { habit ->
            when (habit.frequency) {
                "DAILY" -> true
                "WEEKLY" -> habit.frequencyDays?.contains(Weekday.entries[weekdayIndex]) ?: false
                else -> false
            }
        }

        if (habitsForDay.isEmpty()) {
            // Si no hay hábitos programados, no rompe la racha, pero tampoco la continúa.
            // Para el cálculo que cuenta hacia atrás, esto significa que la racha se detiene aquí.
            return false
        }

        val completions = habitRepository.getHabitsDueTodayWithCompletionCount(userId, date, weekdayIndex).first()

        // Verificamos si TODOS los hábitos programados para hoy están completados.
        return habitsForDay.all { habitToCheck ->
            val completionData = completions.find { (habit, _) -> habit.id == habitToCheck.id }

            if (completionData != null) {
                val (habit, count) = completionData
                when {
                    habit.dailyTarget == null -> count > 0
                    else -> count >= habit.dailyTarget
                }
            } else {
                false // Si el hábito no está en la lista de completados, no se cumplió.
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

                if (completionRate >= 0.7f) {
                    productiveDays++
                }
            }
            checkDate = checkDate.plus(1, DateTimeUnit.DAY)
        }
        return productiveDays
    }
}

// Data classes de ayuda que viven en este mismo fichero

data class BasicDashboardData(
    val user: com.alejandro.habitjourney.features.user.domain.model.User?,
    val todayHabitsWithCount: List<Pair<Habit, Int>>,
    val activeTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>,
    val completedTasksToday: List<com.alejandro.habitjourney.features.task.domain.model.Task>,
    val overdueTasks: List<com.alejandro.habitjourney.features.task.domain.model.Task>
)

data class NotesAndStats(
    val activeNotes: List<com.alejandro.habitjourney.features.note.domain.model.Note>,
    val streak: Int, // <-- Modificado
    val productiveDays: Int
)