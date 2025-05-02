package com.alejandro.habitjourney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alejandro.habitjourney.data.local.entity.HabitEntity
import com.alejandro.habitjourney.data.local.entity.HabitWithLogs
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // Insertar hábito
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    // Actualizar hábito
    @Update
    suspend fun updateHabit(habit: HabitEntity)

    // Eliminar hábito (soft delete)
    @Query("UPDATE habits SET is_deleted = 1 WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)

    // Obtener hábitos activos del usuario
    @Query("""
        SELECT * FROM habits 
        WHERE user_id = :userId 
        AND is_active = 1 
        AND is_deleted = 0
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveHabitsPaged(userId: Long, limit: Int, offset: Int): List<HabitEntity>


    // Obtener hábitos para un día específico
    @Query("""
        SELECT * FROM habits 
        WHERE user_id = :userId 
        AND is_active = 1 
        AND is_deleted = 0
        AND (
            frequency = 'daily' 
            OR (
                frequency = 'weekly' 
                AND (
                    /* Usando una expresión regular para encontrar el número exacto */
                    frequency_days LIKE ',' || :weekdayIndex || ',' 
                    OR frequency_days LIKE :weekdayIndex || ',%' 
                    OR frequency_days LIKE '%,' || :weekdayIndex 
                    OR frequency_days = :weekdayIndex
                )
            )
        )
    """)
    fun getHabitsForDay(userId: Long, weekdayIndex: Int): Flow<List<HabitEntity>>

    // Obtener hábito con sus logs (relación)
    @Transaction
    @Query("""
        SELECT * FROM habits 
        WHERE id = :habitId
    """)
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogs>
}