package com.alejandro.habitjourney.features.task.di


import android.content.Context
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.task.data.local.AlarmPermissionHelper
import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.data.repository.ReminderRepositoryImpl
import com.alejandro.habitjourney.features.task.data.repository.TaskRepositoryImpl
import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import com.alejandro.habitjourney.features.task.domain.usecase.GetAllTasksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    @Provides
    @Singleton
    fun provideAlarmPermissionHelper(
        @ApplicationContext context: Context
    ): AlarmPermissionHelper = AlarmPermissionHelper(context)

    @Provides
    fun provideGetAllTasksUseCase(
        taskRepository: TaskRepository
    ): GetAllTasksUseCase = GetAllTasksUseCase(taskRepository)

    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderManager: ReminderManager
    ): ReminderRepository {
        return ReminderRepositoryImpl(reminderManager)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

}