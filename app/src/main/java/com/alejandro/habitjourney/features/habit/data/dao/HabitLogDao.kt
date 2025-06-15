package com.alejandro.habitjourney.features.habit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * DAO para logs de hábitos con consultas optimizadas para cálculos de rachas y estadísticas.
 */
@Dao
interface HabitLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    /**
     * Inserta múltiples logs en una transacción para operaciones batch.
     */
    @Transaction
    suspend fun insertMultipleLogs(logs: List<HabitLogEntity>) {
        logs.forEach { insertLog(it) }
    }

    /**
     * Obtiene el log de un hábito para una fecha específica.
     * Flow reactivo para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    fun getHabitLogForDate(habitId: Long, date: LocalDate): Flow<HabitLogEntity?>

    /**
     * Obtiene logs en un rango de fechas ordenados por fecha descendente.
     * Útil para gráficos y análisis de tendencias.
     */
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

    /**
     * Consulta optimizada para cálculo de rachas.
     * Obtiene logs desde el pasado hasta la fecha actual ordenados para análisis secuencial.
     */
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

    /**
     * Calcula porcentaje de completación en un rango de fechas.
     * Usa CASE para contar solo logs completados.
     */
    @Query("""
        SELECT 
            COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*) AS completion_rate
        FROM habit_logs
        WHERE habit_id = :habitId
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getCompletionRate(habitId: Long, startDate: LocalDate, endDate: LocalDate): Float

    @Update
    suspend fun updateLog(log: HabitLogEntity)
}