package com.alejandro.habitjourney.features.progress.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.progress.data.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // Insertar progreso (solo si no existe)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgress(progress: ProgressEntity)

    // Actualizar progreso existente
    @Update
    suspend fun updateProgress(progress: ProgressEntity): Int

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