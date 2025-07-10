package com.alejandro.habitjourney.features.habit.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.HabitType
import com.alejandro.habitjourney.core.data.local.enums.LogStatus
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.habit.data.entity.HabitLogEntity
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitLogDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var habitLogDao: HabitLogDao
    private lateinit var userDao: UserDao
    private var userId: String = "0"
    private var habitId: Long = 0

    private val today = TestDataFactory.TODAY

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        habitDao = database.habitDao()
        habitLogDao = database.habitLogDao()
        userDao = database.userDao()

        // Crear un usuario y un hábito para las pruebas
        coroutineRule.runTest {
            val user = TestDataFactory.createUserEntity(
                name = "testuser",
                email = "test@example.com"
            )
            userId = userDao.insertUser(user).toString()

            val habit = TestDataFactory.createHabitEntity(
                userId = userId,
                name = "Test Habit",
                description = "Test description",
                type = HabitType.DO,
                frequency = "daily"
            )
            habitId = habitDao.insertHabit(habit)
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertLog_insertsCorrectLog() = coroutineRule.runTest {
        // Given
        val log = createTestLog()

        // When
        habitLogDao.insertLog(log)

        // Then
        val result = habitLogDao.getLogsForPeriod(habitId, today, today).first()
        assertEquals(1, result.size)
        assertEquals(habitId, result[0].habitId)
        assertEquals(today, result[0].date)
        assertEquals(LogStatus.COMPLETED, result[0].status)
    }

    @Test
    fun insertLogs_insertsMultipleLogs() = coroutineRule.runTest {
        // Given
        val logs = listOf(
            createTestLog(date = today),
            createTestLog(date = today.minus(DatePeriod(days = 1))),
            createTestLog(date = today.minus(DatePeriod(days = 2)))
        )

        // When
        habitLogDao.insertMultipleLogs(logs)

        // Then
        val startDate = today.minus(DatePeriod(days = 3))
        val result = habitLogDao.getLogsForPeriod(habitId, startDate, today).first()
        assertEquals(3, result.size)
    }

    @Test
    fun getLogsForPeriod_returnsCorrectLogs() = coroutineRule.runTest {
        // Given
        val logs = listOf(
            createTestLog(date = today),
            createTestLog(date = today.minus(DatePeriod(days = 1))),
            createTestLog(date = today.minus(DatePeriod(days = 2))),
            createTestLog(date = today.minus(DatePeriod(days = 10)))
        )
        habitLogDao.insertMultipleLogs(logs)

        // When
        val startDate = today.minus(DatePeriod(days = 3))
        val result = habitLogDao.getLogsForPeriod(habitId, startDate, today).first()

        // Then
        assertEquals(3, result.size)
        // Verificar que están ordenados por fecha descendente
        assertTrue(result[0].date >= result[1].date)
        assertTrue(result[1].date >= result[2].date)
    }

    @Test
    fun getLogsForStreakCalculation_returnsLogsBeforeDate() = coroutineRule.runTest {
        // Given
        val logs = listOf(
            createTestLog(date = today),
            createTestLog(date = today.minus(DatePeriod(days = 1))),
            createTestLog(date = today.minus(DatePeriod(days = 2))),
            createTestLog(date = today.plus(DatePeriod(days = 1))) // Futuro, no debería incluirse
        )
        habitLogDao.insertMultipleLogs(logs)

        // When
        val result = habitLogDao.getLogsForStreakCalculation(habitId, today)

        // Then
        assertEquals(3, result.size)
        // Verificar que ningún log es posterior a today
        result.forEach { log ->
            assertTrue(log.date <= today)
        }
    }

    @Test
    fun getCompletionRate_calculatesCorrectRate() = coroutineRule.runTest {
        // Given
        val logs = listOf(
            createTestLog(date = today, status = LogStatus.COMPLETED),
            createTestLog(date = today.minus(DatePeriod(days = 1)), status = LogStatus.COMPLETED),
            createTestLog(date = today.minus(DatePeriod(days = 2)), status = LogStatus.SKIPPED),
            createTestLog(date = today.minus(DatePeriod(days = 3)), status = LogStatus.MISSED)
        )
        habitLogDao.insertMultipleLogs(logs)

        // When
        val startDate = today.minus(DatePeriod(days = 3))
        val completionRate = habitLogDao.getCompletionRate(habitId, startDate, today)

        // Then
        // Esperamos 50% (2 de 4 completados)
        assertEquals(50f, completionRate, 0.1f)
    }

    @Test
    fun updateLog_updatesExistingLog() = coroutineRule.runTest {
        // Given
        val log = createTestLog(status = LogStatus.MISSED)
        habitLogDao.insertLog(log)

        // Get the inserted log to get its ID
        val insertedLog = habitLogDao.getLogsForPeriod(habitId, today, today).first()[0]

        // When - Update to COMPLETED
        val updatedLog = insertedLog.copy(status = LogStatus.COMPLETED)
        habitLogDao.updateLog(updatedLog)

        // Then
        val result = habitLogDao.getLogsForPeriod(habitId, today, today).first()[0]
        assertEquals(LogStatus.COMPLETED, result.status)
    }

    // Función auxiliar para crear un log de prueba usando TestDataFactory
    private fun createTestLog(
        habitId: Long = this.habitId,
        date: LocalDate = today,
        status: LogStatus = LogStatus.COMPLETED,
        value: Float? = null
    ): HabitLogEntity {
        return TestDataFactory.createHabitLogEntity(
            habitId = habitId,
            date = date,
            status = status,
            value = value
        )
    }
}