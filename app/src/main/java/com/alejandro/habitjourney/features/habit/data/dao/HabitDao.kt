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

/**
 * DAO para operaciones CRUD de hábitos con consultas optimizadas para frecuencias.
 * Incluye filtros por usuario, estado archivado y frecuencias complejas.
 */
@Dao
interface HabitDao {

    /**
     * Inserta un nuevo hábito. Retorna el ID generado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    /**
     * Archiva un hábito (soft delete) en lugar de eliminarlo.
     */
    @Query("UPDATE habits SET is_archived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Long)

    @Query("UPDATE habits SET is_archived = 0 WHERE id = :habitId")
    suspend fun unarchiveHabit(habitId: Long)

    /**
     * Obtiene un hábito por ID, excluyendo archivados.
     */
    @Query("""
        SELECT * FROM habits
        WHERE id = :habitId AND is_archived = 0
    """)
    suspend fun getHabitById(habitId: Long): HabitEntity?

    /**
     * Obtiene un hábito por ID sin filtrar archivados (para admin/recovery).
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitByIdUnfiltered(habitId: Long): HabitEntity?

    /**
     * Obtiene todos los hábitos de un usuario, incluyendo archivados.
     */
    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        ORDER BY created_at DESC
    """)
    fun getAllHabitsForUser(userId: Long): Flow<List<HabitEntity>>

    /**
     * Obtiene solo hábitos activos (no archivados) de un usuario.
     */
    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        AND is_archived = 0
        ORDER BY created_at DESC
    """)
    fun getActiveHabitsForUser(userId: Long): Flow<List<HabitEntity>>

    /**
     * Consulta compleja para obtener hábitos que deben realizarse en un día específico.
     * Maneja frecuencias diarias, semanales y personalizadas con validación de fechas.
     *
     * @param userId ID del usuario
     * @param weekdayIndex Día de la semana (0 = Lunes, 6 = Domingo)
     */
    @Query("""
        SELECT * FROM habits
        WHERE user_id = :userId
        AND is_archived = 0
        AND (
            frequency = 'daily'
            OR (
                (frequency = 'weekly' OR frequency = 'custom')
                AND (
                    frequency_days = CAST(:weekdayIndex AS TEXT)
                    OR frequency_days LIKE CAST(:weekdayIndex AS TEXT) || ',%'
                    OR frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT)
                    OR frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT) || ',%'
                )
            )
        )
        AND (
            start_date IS NULL OR start_date <= date('now', 'localtime')
        )
        AND (
            end_date IS NULL OR end_date >= date('now', 'localtime')
        )
    """)
    fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<HabitEntity>>

    /**
     * Obtiene un hábito con todos sus logs usando relación de Room.
     */
    @Transaction
    @Query("""
        SELECT * FROM habits
        WHERE id = :habitId
    """)
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs>

    /**
     * Consulta optimizada para el dashboard que incluye conteo de completaciones del día.
     * JOIN con habit_logs para obtener progreso actual en una sola consulta.
     */
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
                    h.frequency_days = CAST(:weekdayIndex AS TEXT)
                    OR h.frequency_days LIKE CAST(:weekdayIndex AS TEXT) || ',%'
                    OR h.frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT)
                    OR h.frequency_days LIKE '%,' || CAST(:weekdayIndex AS TEXT) || ',%'
                )
            )
        )
        AND (
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