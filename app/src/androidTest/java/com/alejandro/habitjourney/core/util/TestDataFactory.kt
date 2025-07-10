package com.alejandro.habitjourney.core.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
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
        id: String = 1L.toString(), // Como no es autogenerado, le damos un ID fijo para el test
        name: String = "Test User",
        email: String = "test@example.com",
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ): UserEntity {
        return UserEntity(
            id = id,
            name = name,
            email = email,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Crea una entidad de hábito para pruebas
     */
    fun createHabitEntity(
        userId: String,
        id: Long = 0,
        name: String = "Test Habit ${System.currentTimeMillis()}",
        description: String? = "Test description",
        type: HabitType = HabitType.DO,
        frequency: String = "daily",
        frequencyDays: List<Weekday>? = null,
        dailyTarget: Int? = null, // ¡Añadido!
        startDate: LocalDate? = TODAY,
        endDate: LocalDate? = null,
        isArchived: Boolean = false,
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
            dailyTarget = dailyTarget, // ¡Añadido!
            startDate = startDate,
            endDate = endDate,
            isArchived = isArchived,
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
        userId: String,
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
            isArchived = isDeleted,
            createdAt = createdAt
        )
    }

    /**
     * Crea una entidad de nota para pruebas
     */
    fun createNoteEntity(
        userId: String,
        id: Long = 0,
        title: String = "Test Note ${System.currentTimeMillis()}",
        content: String = "Test note content",
        noteType: String = "TEXT",
        listItems: String? = null,
        isArchived: Boolean = false,
        createdAt: Long = TEST_TIMESTAMP,
        updatedAt: Long = TEST_TIMESTAMP,
        wordCount: Int = 0,
        isFavorite: Boolean = false
    ): NoteEntity {
        return NoteEntity(
            id = id,
            userId = userId,
            title = title,
            content = content,
            noteType = noteType,
            listItems = listItems,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            wordCount = wordCount,
            isFavorite = isFavorite
        )
    }
}