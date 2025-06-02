package com.alejandro.habitjourney.features.habit.domain.repository

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HabitRepository {
    suspend fun createHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun archiveHabit(habitId: Long)
    suspend fun unarchiveHabit(habitId: Long)

    suspend fun getHabitById(habitId: Long): Habit?
    suspend fun getHabitByIdUnfiltered(habitId: Long): Habit?

    fun getAllHabitsForUser(userId: Long): Flow<List<Habit>>
    fun getActiveHabitsForUser(userId: Long): Flow<List<Habit>>
    fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<Habit>>
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs>

    fun getLogForDate(habitId: Long, date: LocalDate): Flow<HabitLog?>

    suspend fun toggleHabitArchived(habitId: Long, archive: Boolean)
    suspend fun logHabitCompletion(habitId: Long, date: LocalDate, value: Float, status: LogStatus)
    suspend fun logHabitCompletion(habitLog: HabitLog)
    suspend fun updateHabitLog(habitLog: HabitLog)

    fun getLogsForPeriod(habitId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitLog>>
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Float
    fun getHabitsDueTodayWithCompletionCount(userId: Long, today: LocalDate, weekdayIndex: Int): Flow<List<Pair<Habit, Int>>>

}