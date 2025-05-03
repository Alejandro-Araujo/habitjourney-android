package com.alejandro.habitjourney.features.achievement.data.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import com.alejandro.habitjourney.features.user.data.entity.UserEntity

@Entity(
    tableName = "user_achievements",
    primaryKeys = ["user_id", "achievement_definition_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(
            entity = AchievementDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["achievement_definition_id"],
            onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("user_id"),
        Index("achievement_definition_id")
    ]
)
data class UserAchievementEntity(
    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "achievement_definition_id")
    val achievementDefinitionId: Long,

    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long = System.currentTimeMillis()
)