package com.alejandro.habitjourney.features.achievement.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.achievement.data.entity.AchievementDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDefinitionDao {

    // Insertar definición de logro
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinition(definition: AchievementDefinitionEntity): Long

    // Actualizar definición de logro existente
    @Update
    suspend fun updateDefinition(definition: AchievementDefinitionEntity)

    // Obtener las definiciones
    @Query("SELECT * FROM achievement_definitions")
    fun getAllDefinitions(): Flow<List<AchievementDefinitionEntity>>

    // Obtener definición por código
    @Query("SELECT * FROM achievement_definitions WHERE code = :code LIMIT 1")
    suspend fun getDefinitionByCode(code: String): AchievementDefinitionEntity?

    // Eliminar definición por código
    @Query("DELETE FROM achievement_definitions WHERE code = :code")
    suspend fun deleteDefinitionByCode(code: String): Int

}