package com.alejandro.habitjourney.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alejandro.habitjourney.features.achievement.data.dao.AchievementDefinitionDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.progress.data.dao.ProgressDao
import com.alejandro.habitjourney.features.reward.data.dao.RewardDao
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.achievement.data.dao.UserAchievementDao
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import com.alejandro.habitjourney.features.achievement.data.entity.AchievementDefinitionEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.progress.data.entity.ProgressEntity
import com.alejandro.habitjourney.features.reward.data.entity.RewardEntity
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.achievement.data.entity.UserAchievementEntity
import com.alejandro.habitjourney.features.user.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        ProgressEntity::class,
        AchievementDefinitionEntity::class,
        UserAchievementEntity::class,
        RewardEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun progressDao(): ProgressDao
    abstract fun achievementDefinitionDao(): AchievementDefinitionDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun rewardDao(): RewardDao

}