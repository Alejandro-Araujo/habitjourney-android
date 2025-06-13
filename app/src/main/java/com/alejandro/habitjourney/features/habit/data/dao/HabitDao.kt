package com.alejandro.habitjourney.features.habit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitWithCompletionCount
import com.alejandro.habitjourney.features.habit.data.entity.HabitWithLogs
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET is_archived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("UPDATE habits SET is_archived = 0 WHERE id = :habitId")
    suspend fun unarchiveHabit(habitId: Long)

    @Query("""
        SELECT * FROM habits
        WHERE id = :habitId AND is_archived = 0
    """)
    suspend fun getHabitById(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitByIdUnfiltered(habitId: Long): HabitEntity?

    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        ORDER BY created_at DESC
    """)
    fun getAllHabitsForUser(userId: Long): Flow<List<HabitEntity>>

    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        AND is_archived = 0
        ORDER BY created_at DESC
    """)
    fun getActiveHabitsForUser(userId: Long): Flow<List<HabitEntity>>

    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        AND is_archived = 0
        AND (
            frequency = 'daily'
            OR (
                (frequency = 'weekly' OR frequency = 'custom')
                AND (
                    -- Casos para detectar el weekdayIndex en frequency_days:
                    -- 1. String que contiene solo el número: frequency_days = '1'
                    frequency_days = CAST(:weekdayIndex AS TEXT)
                    -- 2. String que empieza con el número seguido de coma: frequency_days LIKE '1,%'
                    OR frequency_days LIKE CAST(:weekdayIndex AS TEXT) || ',%'
                    -- 3. String que termina con coma seguida del número: frequency_days LIKE '%,1'
                    OR frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT)
                    -- 4. String que contiene el número entre comas: frequency_days LIKE '%,1,%'
                    OR frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT) || ',%'
                )
            )
        )
        AND (
            -- Verificar que el hábito ha comenzado (start_date <= hoy)
            start_date IS NULL OR start_date <= date('now', 'localtime')
        )
        AND (
            -- Verificar que el hábito no ha terminado (end_date >= hoy o es null)
            end_date IS NULL OR end_date >= date('now', 'localtime')
        )
    """)
    fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<HabitEntity>>

    @Transaction
    @Query("""
        SELECT * FROM habits
        WHERE id = :habitId
    """)
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs>

    @Query("""
        SELECT h.*,
               COALESCE(SUM(CASE 
                   WHEN hl.status IN ('COMPLETED', 'PARTIAL') AND hl.date = :today 
                   THEN CAST(hl.value AS INTEGER)
                   ELSE 0 
               END), 0) as currentCompletionCount
        FROM habits AS h
        LEFT JOIN habit_logs AS hl ON h.id = hl.habit_id AND hl.date = :today
        WHERE h.user_id = :userId
        AND h.is_archived = 0
        AND (
            h.frequency = 'daily'
            OR (
                (h.frequency = 'weekly' OR h.frequency = 'custom')
                AND (
                    -- Misma lógica mejorada para frequency_days
                    h.frequency_days = CAST(:weekdayIndex AS TEXT)
                    OR h.frequency_days LIKE CAST(:weekdayIndex AS TEXT) || ',%'
                    OR h.frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT)
                    OR h.frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT) || ',%'
                )
            )
        )
        AND (
            -- Solo incluir hábitos que deberían estar activos hoy
            h.start_date IS NULL OR h.start_date <= :today
        )
        AND (
            h.end_date IS NULL OR h.end_date >= :today
        )
        GROUP BY h.id
        ORDER BY h.created_at DESC
    """)
    fun getHabitsDueTodayWithLogCounts(
        userId: Long,
        today: LocalDate,
        weekdayIndex: Int
    ): Flow<List<HabitWithCompletionCount>>
}