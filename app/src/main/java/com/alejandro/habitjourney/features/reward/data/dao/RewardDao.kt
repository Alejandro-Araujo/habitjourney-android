package com.alejandro.habitjourney.features.reward.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.reward.data.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Insert
    suspend fun insert(reward: RewardEntity): Long

    @Update
    suspend fun update(reward: RewardEntity)

    @Query("SELECT * FROM rewards WHERE user_id = :userId AND is_claimed = 0")
    fun getUnclaimed(userId: Long): Flow<List<RewardEntity>>

    // Marcar como reclamada
    @Query("UPDATE rewards SET is_claimed = 1 WHERE id = :rewardId")
    suspend fun markAsClaimed(rewardId: Long)

    // Obtener todas las recompensas con filtro opcional por estado
    @Query("""
        SELECT * FROM rewards 
        WHERE user_id = :userId
        AND (:showAll = 1 OR is_claimed = :isClaimed)
        ORDER BY created_at DESC
    """)
    fun getRewards(userId: Long, isClaimed: Boolean, showAll: Boolean = false): Flow<List<RewardEntity>>

}