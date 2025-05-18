package com.alejandro.habitjourney.features.habit.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.Weekday
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.habit.data.entity.HabitEntity
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0

    private val today = TestDataFactory.TODAY

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        habitDao = database.habitDao()
        userDao = database.userDao()

        // Crear un usuario para las pruebas
        coroutineRule.runTest {
            val user = TestDataFactory.createUserEntity(
                name = "testuser",
                email = "test@example.com"
            )
            userId = userDao.insertUser(user)
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertHabit_returnsId() = coroutineRule.runTest {
        // Given
        val habit = createTestHabit()

        // When
        val habitId = habitDao.insertHabit(habit)

        // Then
        assertTrue(habitId > 0)
    }

    @Test
    fun updateHabit_updatesCorrectHabit() = coroutineRule.runTest {
        // Given
        val habit = createTestHabit()
        val habitId = habitDao.insertHabit(habit)

        // When
        val updatedHabit = habit.copy(
            id = habitId,
            name = "Updated Habit",
            description = "Updated description"
        )
        habitDao.updateHabit(updatedHabit)

        // Then
        val result = habitDao.getHabitWithLogs(habitId).first()
        assertEquals("Updated Habit", result.habit.name)
        assertEquals("Updated description", result.habit.description)
    }

    @Test
    fun deleteHabit_marksHabitAsDeleted() = coroutineRule.runTest {
        // Given
        val habit = createTestHabit()
        val habitId = habitDao.insertHabit(habit)

        // When
        habitDao.deleteHabit(habitId)

        // Then
        val result = habitDao.getHabitWithLogs(habitId).first()
        assertTrue(result.habit.isDeleted)
    }

    @Test
    fun getActiveHabitsPaged_returnsOnlyActiveHabits() = coroutineRule.runTest {
        // Given
        // Insert active habits
        for (i in 1..5) {
            val habit = createTestHabit(name = "Active Habit $i")
            habitDao.insertHabit(habit)
        }

        // Insert inactive habit
        val inactiveHabit = createTestHabit(name = "Inactive Habit", isActive = false)
        habitDao.insertHabit(inactiveHabit)

        // Insert deleted habit
        val deletedHabit = createTestHabit(name = "Deleted Habit", isDeleted = true)
        habitDao.insertHabit(deletedHabit)

        // When
        val result = habitDao.getActiveHabitsPaged(userId, limit = 10, offset = 0)

        // Then
        assertEquals(5, result.size)
        result.forEach { habit ->
            assertTrue(habit.isActive)
            assertFalse(habit.isDeleted)
        }
    }

    @Test
    fun getHabitsForDay_returnsDailyAndMatchingWeeklyHabits() = coroutineRule.runTest {
        // Given
        // Create a daily habit
        val dailyHabit = createTestHabit(
            name = "Daily Habit",
            frequency = "daily"
        )
        habitDao.insertHabit(dailyHabit)

        // Create a weekly habit for today
        val todayWeekday = today.dayOfWeek.ordinal
        val weeklyHabitForToday = createTestHabit(
            name = "Weekly Habit for Today",
            frequency = "weekly",
            frequencyDays = listOf(Weekday.entries[todayWeekday])
        )
        habitDao.insertHabit(weeklyHabitForToday)

        // Create a weekly habit for another day
        val tomorrowWeekday = (todayWeekday + 1) % 7
        val weeklyHabitForTomorrow = createTestHabit(
            name = "Weekly Habit for Tomorrow",
            frequency = "weekly",
            frequencyDays = listOf(Weekday.entries[tomorrowWeekday])
        )
        habitDao.insertHabit(weeklyHabitForTomorrow)

        // When
        val result = habitDao.getHabitsForDay(userId, todayWeekday).first()

        // Then
        assertEquals(2, result.size)
        val habitNames = result.map { it.name }
        assertTrue("Daily Habit" in habitNames)
        assertTrue("Weekly Habit for Today" in habitNames)
        assertTrue("Weekly Habit for Tomorrow" !in habitNames)
    }

    @Test
    fun getHabitWithLogs_returnsHabitWithCorrectLogs() = coroutineRule.runTest {
        // Given
        val habit = createTestHabit()
        val habitId = habitDao.insertHabit(habit)

        // Logs will be added in HabitLogDaoTest

        // When
        val result = habitDao.getHabitWithLogs(habitId).first()

        // Then
        assertEquals(habitId, result.habit.id)
        assertEquals("Test Habit", result.habit.name)
        assertEquals(0, result.logs.size)  // No logs added yet
    }

    // Función auxiliar para crear un hábito de prueba usando TestDataFactory
    private fun createTestHabit(
        name: String = "Test Habit",
        description: String = "Test description",
        type: HabitType = HabitType.DO,
        frequency: String = "daily",
        frequencyDays: List<Weekday>? = null,
        isActive: Boolean = true,
        isDeleted: Boolean = false
    ): HabitEntity {
        return TestDataFactory.createHabitEntity(
            userId = userId,
            name = name,
            description = description,
            type = type,
            frequency = frequency,
            frequencyDays = frequencyDays,
            startDate = today,
            endDate = null,
            isActive = isActive,
            isDeleted = isDeleted
        )
    }
}