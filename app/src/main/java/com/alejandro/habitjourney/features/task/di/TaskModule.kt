package com.alejandro.habitjourney.features.task.di

import android.content.Context
import com.alejandro.habitjourney.features.task.data.dao.TaskDao
import com.alejandro.habitjourney.features.task.data.local.AlarmPermissionHelper
import com.alejandro.habitjourney.features.task.data.local.ReminderManager
import com.alejandro.habitjourney.features.task.data.repository.ReminderRepositoryImpl
import com.alejandro.habitjourney.features.task.data.repository.TaskRepositoryImpl
import com.alejandro.habitjourney.features.task.domain.repository.ReminderRepository
import com.alejandro.habitjourney.features.task.domain.repository.TaskRepository
import com.alejandro.habitjourney.features.task.domain.usecase.ArchiveTaskUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.CreateTaskUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.DeleteTaskUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetActiveTasksUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetAllTasksUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetArchivedTasksUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetCompletedTasksUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetOverdueTasksUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.GetTaskByIdUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.ToggleTaskCompletionUseCase
import com.alejandro.habitjourney.features.task.domain.usecase.UpdateTaskUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Dagger Hilt que proporciona las dependencias relacionadas con las tareas
 * en el árbol de dependencias de la aplicación.
 * Este módulo está instalado en el [SingletonComponent], lo que significa que
 * las instancias proporcionadas serán singlenton a lo largo de la vida de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    /**
     * Provee una instancia de [AlarmPermissionHelper].
     * @param context El contexto de la aplicación, inyectado por Hilt.
     * @return Una instancia única de [AlarmPermissionHelper].
     */
    @Provides
    @Singleton
    fun provideAlarmPermissionHelper(
        @ApplicationContext context: Context
    ): AlarmPermissionHelper = AlarmPermissionHelper(context)

    /**
     * Provee una instancia de [ReminderRepository].
     * @param reminderManager El gestor de recordatorios necesario para el repositorio.
     * @return Una instancia única de [ReminderRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideReminderRepository(
        reminderManager: ReminderManager
    ): ReminderRepository {
        return ReminderRepositoryImpl(reminderManager)
    }

    /**
     * Provee una instancia de [TaskRepository].
     * @param taskDao El DAO de tareas necesario para el repositorio.
     * @return Una instancia única de [TaskRepositoryImpl].
     */
    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }

    // --- Casos de Uso (Use Cases) ---

    @Provides
    fun provideGetAllTasksUseCase(
        taskRepository: TaskRepository
    ): GetAllTasksUseCase = GetAllTasksUseCase(taskRepository)

    @Provides
    fun provideGetPendingTasksUseCase(
        taskRepository: TaskRepository
    ): GetActiveTasksUseCase = GetActiveTasksUseCase(taskRepository)

    @Provides
    fun provideGetCompletedTasksUseCase(
        taskRepository: TaskRepository
    ): GetCompletedTasksUseCase = GetCompletedTasksUseCase(taskRepository)

    @Provides
    fun provideGetTaskByIdUseCase(
        taskRepository: TaskRepository
    ): GetTaskByIdUseCase = GetTaskByIdUseCase(taskRepository)

    @Provides
    fun provideCreateTaskUseCase(
        taskRepository: TaskRepository
    ): CreateTaskUseCase = CreateTaskUseCase(taskRepository)

    @Provides
    fun provideUpdateTaskUseCase(
        taskRepository: TaskRepository
    ): UpdateTaskUseCase = UpdateTaskUseCase(taskRepository)

    @Provides
    fun provideDeleteTaskUseCase(
        taskRepository: TaskRepository
    ): DeleteTaskUseCase = DeleteTaskUseCase(taskRepository)

    @Provides
    fun provideToggleTaskCompletionUseCase(
        taskRepository: TaskRepository
    ): ToggleTaskCompletionUseCase = ToggleTaskCompletionUseCase(taskRepository)

    @Provides
    fun provideGetTaskStatsUseCase(
        taskRepository: TaskRepository
    ): GetArchivedTasksUseCase = GetArchivedTasksUseCase(taskRepository)

    @Provides
    fun provideGetTodayTasksUseCase(
        taskRepository: TaskRepository
    ): ArchiveTaskUseCase = ArchiveTaskUseCase(taskRepository)

    @Provides
    fun provideGetOverdueTasksUseCase(
        taskRepository: TaskRepository
    ): GetOverdueTasksUseCase = GetOverdueTasksUseCase(taskRepository)

}