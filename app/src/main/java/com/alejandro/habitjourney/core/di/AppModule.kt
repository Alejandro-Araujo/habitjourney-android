package com.alejandro.habitjourney.core.di

import android.content.Context
import androidx.room.Room
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.features.achievement.data.dao.AchievementDefinitionDao
import com.alejandro.habitjourney.features.achievement.data.dao.UserAchievementDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.progress.data.dao.ProgressDao
import com.alejandro.habitjourney.features.reward.data.dao.RewardDao
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Coroutine Scope para operaciones a nivel de aplicación
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    // Room Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "habitjourney_db"
        )
            .fallbackToDestructiveMigration()  // Solo para desarrollo
            .build()
    }

    // DAOs
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideHabitDao(database: AppDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideHabitLogDao(database: AppDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideProgressDao(database: AppDatabase): ProgressDao = database.progressDao()

    @Provides
    @Singleton
    fun provideAchievementDefinitionDao(database: AppDatabase): AchievementDefinitionDao =
        database.achievementDefinitionDao()

    @Provides
    @Singleton
    fun provideUserAchievementDao(database: AppDatabase): UserAchievementDao =
        database.userAchievementDao()

    @Provides
    @Singleton
    fun provideRewardDao(database: AppDatabase): RewardDao = database.rewardDao()

}