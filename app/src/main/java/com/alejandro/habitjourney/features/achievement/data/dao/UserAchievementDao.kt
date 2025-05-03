package com.alejandro.habitjourney.features.achievement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandro.habitjourney.features.achievement.data.entity.UserAchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAchievementDao {

    // Asignar logro a usuario
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun assignAchievement(userAchievement: UserAchievementEntity): Long

    // Obtener logros del usuario
    @Query("""
        SELECT * FROM user_achievements 
        WHERE user_id = :userId
        ORDER BY unlocked_at DESC
    """)
    fun getAchievementsForUser(userId: Long): Flow<List<UserAchievementEntity>>

    // Comprobar si un logro ya ha sido desbloqueado
    @Query("""
        SELECT * FROM user_achievements 
        WHERE user_id = :userId 
        AND achievement_definition_id = :achievementId
    """)
    suspend fun isAchievementUnlocked(userId: Long, achievementId: Long): UserAchievementEntity?

    @Query("SELECT * FROM user_achievements WHERE user_id = :userId")
    fun getByUser(userId: Long): Flow<List<UserAchievementEntity>>

    // Estadísticas de logros - porcentaje de logros desbloqueados
    @Query("""
        SELECT 
            (COUNT(ua.user_id) * 100.0 / (SELECT COUNT(*) FROM achievement_definitions)) AS percentage
        FROM user_achievements ua
        WHERE ua.user_id = :userId
    """)
    suspend fun getAchievementCompletionPercentage(userId: Long): Float

    // Obtener logros recientes
    @Query("""
        SELECT * FROM user_achievements
        WHERE user_id = :userId
        ORDER BY unlocked_at DESC
        LIMIT :limit
    """)
    fun getRecentAchievements(userId: Long, limit: Int): Flow<List<UserAchievementEntity>>

}