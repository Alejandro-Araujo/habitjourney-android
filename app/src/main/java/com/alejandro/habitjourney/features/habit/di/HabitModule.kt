package com.alejandro.habitjourney.features.habit.di

import com.alejandro.habitjourney.features.habit.data.repository.HabitRepositoryImpl
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.habit.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la inyección de dependencias del feature de Hábitos.
 *
 * Proporciona la implementación del repositorio [HabitRepository] y todos los casos de uso
 * relacionados con la gestión de hábitos, asegurando que estén disponibles en el grafo
 * de dependencias de la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object HabitModule {

    /**
     * Proporciona una instancia única (Singleton) de [HabitRepository].
     *
     * @param habitDao DAO para operaciones de hábitos.
     * @param habitLogDao DAO para operaciones de registros de hábitos.
     * @param habitLocalMapper Mapeador entre entidades locales y modelos de dominio.
     * @return Una implementación de [HabitRepository].
     */
    @Provides
    @Singleton
    fun provideHabitRepository(
        habitDao: com.alejandro.habitjourney.features.habit.data.dao.HabitDao,
        habitLogDao: com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao,
        habitLocalMapper: com.alejandro.habitjourney.features.habit.data.mapper.HabitLocalMapper
    ): HabitRepository {
        return HabitRepositoryImpl(habitDao, habitLogDao, habitLocalMapper)
    }

    // --- Casos de Uso (Use Cases) ---

    @Provides
    fun provideCreateHabitUseCase(repository: HabitRepository): CreateHabitUseCase =
        CreateHabitUseCase(repository)

    @Provides
    fun provideUpdateHabitUseCase(repository: HabitRepository): UpdateHabitUseCase =
        UpdateHabitUseCase(repository)

    @Provides
    fun provideGetActiveHabitsUseCase(repository: HabitRepository): GetActiveHabitsUseCase =
        GetActiveHabitsUseCase(repository)

    @Provides
    fun provideGetAllUserHabitsUseCase(repository: HabitRepository): GetAllUserHabitsUseCase =
        GetAllUserHabitsUseCase(repository)

    @Provides
    fun provideGetHabitByIdUseCase(repository: HabitRepository): GetHabitByIdUseCase =
        GetHabitByIdUseCase(repository)

    @Provides
    fun provideGetHabitWithLogsUseCase(repository: HabitRepository): GetHabitWithLogsUseCase =
        GetHabitWithLogsUseCase(repository)

    @Provides
    fun provideGetHabitsDueTodayWithCompletionCountUseCase(repository: HabitRepository): GetHabitsDueTodayWithCompletionCountUseCase =
        GetHabitsDueTodayWithCompletionCountUseCase(repository)

    @Provides
    fun provideGetLogForDateUseCase(repository: HabitRepository): GetLogForDateUseCase =
        GetLogForDateUseCase(repository)

    @Provides
    fun provideLogHabitCompletionUseCase(repository: HabitRepository): LogHabitCompletionUseCase =
        LogHabitCompletionUseCase(repository)

    @Provides
    fun provideMarkHabitAsNotCompletedUseCase(
        repository: HabitRepository,
        getLogForDateUseCase: GetLogForDateUseCase
    ): MarkHabitAsNotCompletedUseCase =
        MarkHabitAsNotCompletedUseCase(repository, getLogForDateUseCase)

    @Provides
    fun provideMarkHabitAsSkippedUseCase(repository: HabitRepository): MarkHabitAsSkippedUseCase =
        MarkHabitAsSkippedUseCase(repository)

    @Provides
    fun provideMarkMissedHabitsUseCase(repository: HabitRepository): MarkMissedHabitsUseCase =
        MarkMissedHabitsUseCase(repository)

    @Provides
    fun provideToggleHabitArchivedUseCase(repository: HabitRepository): ToggleHabitArchivedUseCase =
        ToggleHabitArchivedUseCase(repository)

    @Provides
    fun provideUpdateHabitProgressValueUseCase(repository: HabitRepository): UpdateHabitProgressValueUseCase =
        UpdateHabitProgressValueUseCase(repository)
}
