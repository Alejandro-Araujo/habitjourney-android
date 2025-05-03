package com.alejandro.habitjourney.features.habit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface HabitLogDao {

    @Insert
    suspend fun insertLog(log: HabitLogEntity)

    // Insertar múltiples logs en una transacción
    @Insert
    @Transaction
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    // Obtener logs de un hábito en un rango de fechas
    @Query("""
        SELECT * FROM habit_logs 
        WHERE habit_id = :habitId 
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getLogsForPeriod(
        habitId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HabitLogEntity>>

    @Query("""
        SELECT id, habit_id, date, status, value, created_at
        FROM habit_logs
        WHERE habit_id = :habitId
        AND date <= :currentDate
        ORDER BY date DESC
    """)
    suspend fun getLogsForStreakCalculation(
        habitId: Long,
        currentDate: LocalDate
    ): List<HabitLogEntity>

    // Estadísticas: Tasa de completado en un período
    @Query("""
        SELECT 
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*) AS completion_rate
        FROM habit_logs
        WHERE habit_id = :habitId
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Float

    // Actualizar un log existente
    @Update
    suspend fun updateLog(log: HabitLogEntity)
}