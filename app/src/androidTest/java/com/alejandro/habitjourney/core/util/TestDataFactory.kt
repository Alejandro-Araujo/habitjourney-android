package com.alejandro.habitjourney.core.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.features.achievement.data.entity.AchievementDefinitionEntity
import com.alejandro.habitjourney.features.achievement.data.entity.UserAchievementEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.progress.data.entity.ProgressEntity
import com.alejandro.habitjourney.features.reward.data.entity.RewardEntity
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.user.data.entity.UserEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Factory object para crear datos de prueba en tests de DAOs y repositorios.
 * Proporciona métodos para crear entidades y bases de datos en memoria.
 */
object TestDataFactory {

    // Fecha actual para uso en pruebas
    val TODAY: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Timestamp constante para tests que requieren valores consistentes
    const val TEST_TIMESTAMP: Long = 1683721200000 // 2023-05-10T12:00:00Z

    /**
     * Crea una base de datos en memoria para pruebas
     */
    fun createInMemoryDatabase(context: Context = ApplicationProvider.getApplicationContext()): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
    }

    /**
     * Crea una entidad de usuario para pruebas
     */
    fun createUserEntity(
        id: Long = 0, // 0 para que Room asigne un ID automáticamente
        name: String = "Test User ${System.currentTimeMillis()}",
        email: String = "test${System.currentTimeMillis()}@example.com",
        passwordHash: String = "hashed_password",
        createdAt: Long = TEST_TIMESTAMP
    ): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            passwordHash = passwordHash,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de hábito para pruebas
     */
    fun createHabitEntity(
        userId: Long,
        id: Long = 0,
        name: String = "Test Habit ${System.currentTimeMillis()}",
        description: String? = "Test description",
        type: HabitType = HabitType.DO,
        frequency: String = "daily",
        frequencyDays: List<Weekday>? = null,
        startDate: LocalDate? = TODAY,
        endDate: LocalDate? = null,
        isActive: Boolean = true,
        isDeleted: Boolean = false,
        createdAt: Long = TEST_TIMESTAMP
    ): HabitEntity {
        return HabitEntity(
            id = id,
            userId = userId,
            name = name,
            description = description,
            type = type,
            frequency = frequency,
            frequencyDays = frequencyDays,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            isDeleted = isDeleted,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de registro de hábito para pruebas
     */
    fun createHabitLogEntity(
        habitId: Long,
        id: Long = 0,
        date: LocalDate = TODAY,
        status: LogStatus = LogStatus.COMPLETED,
        value: Float? = null,
        createdAt: Long = TEST_TIMESTAMP
    ): HabitLogEntity {
        return HabitLogEntity(
            id = id,
            habitId = habitId,
            date = date,
            status = status,
            value = value,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de tarea para pruebas
     */
    fun createTaskEntity(
        userId: Long,
        id: Long = 0,
        title: String = "Test Task ${System.currentTimeMillis()}",
        description: String? = "Test task description",
        dueDate: LocalDate? = TODAY,
        priority: Priority? = null,
        isCompleted: Boolean = false,
        isDeleted: Boolean = false,
        createdAt: Long = TEST_TIMESTAMP
    ): TaskEntity {
        return TaskEntity(
            id = id,
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            isCompleted = isCompleted,
            isDeleted = isDeleted,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de nota para pruebas
     */
    fun createNoteEntity(
        userId: Long,
        id: Long = 0,
        title: String = "Test Note ${System.currentTimeMillis()}",
        content: String = "Test note content",
        isArchived: Boolean = false,
        isDeleted: Boolean = false,
        createdAt: Long = TEST_TIMESTAMP
    ): NoteEntity {
        return NoteEntity(
            id = id,
            userId = userId,
            title = title,
            content = content,
            isArchived = isArchived,
            isDeleted = isDeleted,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de recompensa para pruebas
     */
    fun createRewardEntity(
        userId: Long,
        id: Long = 0,
        name: String = "Test Reward ${System.currentTimeMillis()}",
        description: String? = "Test reward description",
        isClaimed: Boolean = false,
        createdAt: Long = TEST_TIMESTAMP
    ): RewardEntity {
        return RewardEntity(
            id = id,
            userId = userId,
            name = name,
            description = description,
            isClaimed = isClaimed,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de progreso para pruebas
     */
    fun createProgressEntity(
        userId: Long,
        totalHabitsCompleted: Int = 0,
        totalXp: Int = 0,
        currentStreak: Int = 0,
        longestStreak: Int = 0

    ): ProgressEntity {
        return ProgressEntity(
            userId = userId,
            totalHabitsCompleted = totalHabitsCompleted,
            totalXp = totalXp,
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }

    /**
     * Crea una entidad de definición de logro para pruebas
     */
    fun createAchievementDefinitionEntity(
        id: Long = 0, // 0 para que Room asigne un ID automáticamente
        code: String = "ACH_CODE_${System.currentTimeMillis()}",
        name: String = "Test Achievement Name",
        description: String = "Test achievement description"
    ): AchievementDefinitionEntity {
        return AchievementDefinitionEntity(
            id = id,
            code = code,
            name = name,
            description = description
        )
    }

    /**
     * Crea una entidad de logro de usuario para pruebas
     */
    fun createUserAchievementEntity(
        userId: Long,
        achievementDefinitionId: Long,
        unlockedAt: Long = TEST_TIMESTAMP
    ): UserAchievementEntity {
        return UserAchievementEntity(
            userId = userId,
            achievementDefinitionId = achievementDefinitionId,
            unlockedAt = unlockedAt
        )
    }
}