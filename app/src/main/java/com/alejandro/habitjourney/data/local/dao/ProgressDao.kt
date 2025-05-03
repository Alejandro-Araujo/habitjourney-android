package com.alejandro.habitjourney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandro.habitjourney.data.local.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // Insertar progreso inicial  si ya existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ProgressEntity): Long

    // Obtener progreso de un usuario
    @Query("SELECT * FROM progress WHERE user_id = :userId")
    fun getProgressForUser(userId: Long): Flow<ProgressEntity>

    // Métodos para actualizar valores específicos
    @Query("UPDATE progress SET total_habits_completed = total_habits_completed + 1 WHERE user_id = :userId")
    suspend fun incrementHabitsCompleted(userId: Long)

    @Query("UPDATE progress SET total_xp = total_xp + :amount WHERE user_id = :userId")
    suspend fun addXp(userId: Long, amount: Int)

    @Query("UPDATE progress SET longest_streak = :newStreak WHERE user_id = :userId AND longest_streak < :newStreak")
    suspend fun updateLongestStreakIfGreater(userId: Long, newStreak: Int)

    @Query("UPDATE progress SET current_streak = :streak WHERE user_id = :userId")
    suspend fun updateCurrentStreak(userId: Long, streak: Int)

}