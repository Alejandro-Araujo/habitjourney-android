package com.alejandro.habitjourney.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alejandro.habitjourney.data.local.dao.AchievementDefinitionDao
import com.alejandro.habitjourney.data.local.dao.HabitDao
import com.alejandro.habitjourney.data.local.dao.HabitLogDao
import com.alejandro.habitjourney.data.local.dao.NoteDao
import com.alejandro.habitjourney.data.local.dao.ProgressDao
import com.alejandro.habitjourney.data.local.dao.RewardDao
import com.alejandro.habitjourney.data.local.dao.TaskDao
import com.alejandro.habitjourney.data.local.dao.UserAchievementDao
import com.alejandro.habitjourney.data.local.dao.UserDao
import com.alejandro.habitjourney.data.local.entity.AchievementDefinitionEntity
import com.alejandro.habitjourney.data.local.entity.HabitEntity
import com.alejandro.habitjourney.data.local.entity.HabitLogEntity
import com.alejandro.habitjourney.data.local.entity.NoteEntity
import com.alejandro.habitjourney.data.local.entity.ProgressEntity
import com.alejandro.habitjourney.data.local.entity.RewardEntity
import com.alejandro.habitjourney.data.local.entity.TaskEntity
import com.alejandro.habitjourney.data.local.entity.UserAchievementEntity
import com.alejandro.habitjourney.data.local.entity.UserEntity

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
    exportSchema = true
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