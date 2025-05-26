package com.alejandro.habitjourney.features.habit.di


import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.habit.data.mapper.HabitLocalMapper
import com.alejandro.habitjourney.features.habit.data.repository.HabitRepositoryImpl
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HabitModule {

    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: HabitDao,
        habitLogDao: HabitLogDao,
        habitLocalMapper: HabitLocalMapper
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao, habitLogDao, habitLocalMapper)
    }
}