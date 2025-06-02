package com.alejandro.habitjourney.features.dashboard.di

import com.alejandro.habitjourney.features.dashboard.data.repository.DashboardRepositoryImpl
import com.alejandro.habitjourney.features.dashboard.domain.repository.DashboardRepository
import com.alejandro.habitjourney.features.dashboard.domain.usecase.GetDashboardDataUseCase
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DashboardModule {

    @Provides
    @Singleton
    fun provideGetDashboardDataUseCase(
        userPreferences: UserPreferences,
        userRepository: UserRepository,
        habitRepository: HabitRepository,
        taskRepository: TaskRepository,
        noteRepository: NoteRepository
    ): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(
            userPreferences = userPreferences,
            userRepository = userRepository,
            habitRepository = habitRepository,
            taskRepository = taskRepository,
            noteRepository = noteRepository
        )
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(
        getDashboardDataUseCase: GetDashboardDataUseCase
    ): DashboardRepository {
        return DashboardRepositoryImpl(getDashboardDataUseCase)
    }
}