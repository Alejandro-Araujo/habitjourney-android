package com.alejandro.habitjourney.features.habit.di


import com.alejandro.habitjourney.features.habit.data.dao.HabitDao
import com.alejandro.habitjourney.features.habit.data.dao.HabitLogDao
import com.alejandro.habitjourney.features.habit.data.mapper.HabitLocalMapper
import com.alejandro.habitjourney.features.habit.data.repository.HabitRepositoryImpl
import com.alejandro.habitjourney.features.habit.domain.repository.HabitRepository
import com.alejandro.habitjourney.features.habit.domain.usecase.CreateHabitUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetActiveHabitsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetAllUserHabitsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitByIdUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitWithLogsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetHabitsDueTodayWithCompletionCountUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.GetLogForDateUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.LogHabitCompletionUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsNotCompletedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkHabitAsSkippedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.MarkMissedHabitsUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.ToggleHabitArchivedUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitProgressValueUseCase
import com.alejandro.habitjourney.features.habit.domain.usecase.UpdateHabitUseCase
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

    // Use Cases
    @Provides
    fun provideGetHabitsForDayUseCase(
        habitRepository: HabitRepository
    ): GetActiveHabitsUseCase = GetActiveHabitsUseCase(habitRepository)

    @Provides
    fun provideGetTodayHabitsUseCase(
        habitRepository: HabitRepository
    ): LogHabitCompletionUseCase = LogHabitCompletionUseCase(habitRepository)

    @Provides
    fun provideGetHabitByIdUseCase(
        habitRepository: HabitRepository
    ): GetHabitByIdUseCase = GetHabitByIdUseCase(habitRepository)

    @Provides
    fun provideCreateHabitUseCase(
        habitRepository: HabitRepository
    ): CreateHabitUseCase = CreateHabitUseCase(habitRepository)

    @Provides
    fun provideUpdateHabitUseCase(
        habitRepository: HabitRepository
    ): UpdateHabitUseCase = UpdateHabitUseCase(habitRepository)

    @Provides
    fun provideToggleHabitCompletionUseCase(
        habitRepository: HabitRepository
    ): GetAllUserHabitsUseCase = GetAllUserHabitsUseCase(habitRepository)

    @Provides
    fun provideGetHabitLogsUseCase(
        habitRepository: HabitRepository
    ): GetHabitsDueTodayWithCompletionCountUseCase = GetHabitsDueTodayWithCompletionCountUseCase(habitRepository)

    @Provides
    fun provideGetHabitCompletionRateUseCase(
        habitRepository: HabitRepository
    ): UpdateHabitProgressValueUseCase = UpdateHabitProgressValueUseCase(habitRepository)

    @Provides
    fun provideGetHabitStreakUseCase(
        habitRepository: HabitRepository
    ): GetHabitWithLogsUseCase = GetHabitWithLogsUseCase(habitRepository)

    @Provides
    fun provideGetWeeklyHabitsUseCase(
        habitRepository: HabitRepository,
        getLogForDateUseCase: GetLogForDateUseCase
    ): MarkHabitAsNotCompletedUseCase = MarkHabitAsNotCompletedUseCase(habitRepository, getLogForDateUseCase)

    @Provides
    fun provideGetMonthlyHabitsUseCase(
        habitRepository: HabitRepository
    ): GetLogForDateUseCase = GetLogForDateUseCase(habitRepository)

    @Provides
    fun provideGetHabitStatsUseCase(
        habitRepository: HabitRepository
    ): MarkHabitAsSkippedUseCase = MarkHabitAsSkippedUseCase(habitRepository)

    @Provides
    fun provideMarkMissedHabitsUseCase(
        habitRepository: HabitRepository
    ): MarkMissedHabitsUseCase = MarkMissedHabitsUseCase(habitRepository)

    @Provides
    fun provideToggleHabitArchivedUseCase(
        habitRepository: HabitRepository
    ): ToggleHabitArchivedUseCase = ToggleHabitArchivedUseCase(habitRepository)
}