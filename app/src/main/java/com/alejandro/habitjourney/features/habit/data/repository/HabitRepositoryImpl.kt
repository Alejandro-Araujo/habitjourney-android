package com.alejandro.habitjourney.features.habit.data.repository

import com.alejandro.habitjourney.features.habit.domain.model.Habit
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.habit.data.mapper.HabitLocalMapper
import com.alejandro.habitjourney.features.habit.domain.model.HabitLog
import com.alejandro.habitjourney.features.habit.domain.model.HabitWithLogs
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitMapper: HabitLocalMapper
) : HabitRepository {

    override suspend fun createHabit(habit: Habit): Long {
        return habitDao.insertHabit(habitMapper.habitDomainToEntity(habit))
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habitMapper.habitDomainToEntity(habit))
    }

    override suspend fun archiveHabit(habitId: Long) {
        habitDao.archiveHabit(habitId)
    }

    override suspend fun unarchiveHabit(habitId: Long) {
        habitDao.unarchiveHabit(habitId)
    }

    override suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)?.let { habitMapper.habitEntityToDomain(it) }
    }

    override suspend fun getHabitByIdUnfiltered(habitId: Long): Habit? {
        return habitDao.getHabitByIdUnfiltered(habitId)?.let { habitMapper.habitEntityToDomain(it) }
    }

    override fun getAllHabitsForUser(userId: Long): Flow<List<Habit>> {
        return habitDao.getAllHabitsForUser(userId).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    override fun getActiveHabitsForUser(userId: Long): Flow<List<Habit>> {
        // Esta función del DAO ya filtra por is_archived = 0
        return habitDao.getActiveHabitsForUser(userId).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    override fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<Habit>> {
        return habitDao.getHabitsForDay(userId, weekdayIndex).map { entities ->
            habitMapper.habitEntityListToDomain(entities)
        }
    }

    override fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs> {
        return habitDao.getHabitWithLogs(habitId).map { habitWithLogsEntity ->
            HabitWithLogs(
                habit = habitMapper.habitEntityToDomain(habitWithLogsEntity.habit),
                logs = habitMapper.habitLogEntityListToDomain(habitWithLogsEntity.logs)
            )
        }
    }

    override suspend fun toggleHabitArchived(habitId: Long, archive: Boolean) {
        if (archive) {
            habitDao.archiveHabit(habitId)
        } else {
            habitDao.unarchiveHabit(habitId)
        }
    }

    override suspend fun logHabitCompletion(habitId: Long, date: LocalDate, value: Float, status: LogStatus) {
        val existingLog = habitLogDao.getHabitLogForDate(habitId, date).first()

        if (existingLog != null) {
            val updatedLog = existingLog.copy(value = value, status = status)
            habitLogDao.updateLog(updatedLog)
        } else {
            val newLog = HabitLogEntity(
                habitId = habitId,
                date = date,
                value = value,
                status = status
            )
            habitLogDao.insertLog(newLog)
        }
    }

    override suspend fun logHabitCompletion(habitLog: HabitLog) {
        val existingLog = habitLogDao.getHabitLogForDate(habitLog.habitId, habitLog.date).first()

        if (existingLog != null) {
            val updatedLog = existingLog.copy(
                value = habitLog.value,
                status = habitLog.status
            )
            habitLogDao.updateLog(updatedLog)
        } else {
            habitLogDao.insertLog(habitMapper.habitLogDomainToEntity(habitLog))
        }
    }

    override suspend fun updateHabitLog(habitLog: HabitLog) {
        habitLogDao.updateLog(habitMapper.habitLogDomainToEntity(habitLog))
    }

    override fun getLogsForPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitLog>> {
        return habitLogDao.getLogsForPeriod(habitId, startDate, endDate).map { entities ->
            habitMapper.habitLogEntityListToDomain(entities)
        }
    }

    override fun getLogForDate(habitId: Long, date: LocalDate): Flow<HabitLog?> {
        return habitLogDao.getHabitLogForDate(habitId, date).map { it?.let { habitMapper.habitLogEntityToDomain(it) } }
    }

    override suspend fun getCompletionRate(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float {
        return habitLogDao.getCompletionRate(habitId, startDate, endDate)
    }

    override fun getHabitsDueTodayWithCompletionCount(
        userId: Long,
        today: LocalDate,
        weekdayIndex: Int
    ): Flow<List<Pair<Habit, Int>>> {
        return habitDao.getHabitsDueTodayWithLogCounts(userId, today, weekdayIndex).map { list ->
            list.map { habitWithCompletionCount ->
                Pair(habitMapper.habitEntityToDomain(habitWithCompletionCount.habit), habitWithCompletionCount.currentCompletionCount)
            }
        }
    }
}