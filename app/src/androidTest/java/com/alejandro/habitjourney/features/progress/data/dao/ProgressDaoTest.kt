package com.alejandro.habitjourney.features.progress.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.progress.data.entity.ProgressEntity
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var progressDao: ProgressDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        progressDao = database.progressDao()
        userDao = database.userDao()

        // Crear un usuario para las pruebas
        coroutineRule.runTest {
            val user = TestDataFactory.createUserEntity(
                name = "testuser",
                email = "test@example.com"
            )
            val generatedUserId = userDao.insertUser(user)
            userId = generatedUserId
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertsNewProgress() = coroutineRule.runTest {
        // Given: Una entidad Progress con el userId correcto
        val progress = createTestProgress(userId = userId)

        // When: Intentamos insertar (ignorará si ya existe)
        progressDao.insertProgress(progress)

        // Then:
        val retrievedProgress = progressDao.getProgressForUser(userId).first()

        assertNotNull(retrievedProgress)
        assertEquals(userId, retrievedProgress.userId)
        assertEquals(0, retrievedProgress.totalHabitsCompleted)
        assertEquals(0, retrievedProgress.totalXp)
        assertEquals(0, retrievedProgress.currentStreak)
        assertEquals(0, retrievedProgress.longestStreak)
    }

    @Test
    fun updatesExistingProgress() = coroutineRule.runTest {
        // Given: Una entidad Progress inicial insertada
        val initialProgress = createTestProgress(totalXp = 100)
        progressDao.insertProgress(initialProgress)

        // When:
        val updatedProgress = initialProgress.copy(totalXp = 200)
        val updateCount = progressDao.updateProgress(updatedProgress)

        // Then: Verificamos que la actualización afectó 1 fila
        assertEquals(1, updateCount)
        val result = progressDao.getProgressForUser(userId).first()
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(200, result.totalXp)
    }

    @Test
    fun getProgressForUser_returnsCorrectProgress() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(
            totalHabitsCompleted = 5,
            totalXp = 150,
            currentStreak = 3,
            longestStreak = 7
        )
        progressDao.insertProgress(progress)

        // When
        val result = progressDao.getProgressForUser(userId).first()

        // Then
        assertEquals(userId, result.userId)
        assertEquals(5, result.totalHabitsCompleted)
        assertEquals(150, result.totalXp)
        assertEquals(3, result.currentStreak)
        assertEquals(7, result.longestStreak)
    }

    @Test
    fun incrementHabitsCompleted_increasesTotalHabitsCompleted() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(totalHabitsCompleted = 5)
        progressDao.insertProgress(progress)

        // When
        progressDao.incrementHabitsCompleted(userId)

        // Then
        val result = progressDao.getProgressForUser(userId).first()
        assertEquals(6, result.totalHabitsCompleted)
    }

    @Test
    fun addXp_increasesTotalXp() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(totalXp = 100)
        progressDao.insertProgress(progress)

        // When
        progressDao.addXp(userId, 50)

        // Then
        val result = progressDao.getProgressForUser(userId).first()
        assertEquals(150, result.totalXp)
    }

    @Test
    fun updateLongestStreakIfGreater_updatesWhenGreater() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(longestStreak = 7)
        progressDao.insertProgress(progress)

        // When
        progressDao.updateLongestStreakIfGreater(userId, 10)

        // Then
        val result = progressDao.getProgressForUser(userId).first()
        assertEquals(10, result.longestStreak)
    }

    @Test
    fun updateLongestStreakIfGreater_doesNotUpdateWhenLess() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(longestStreak = 7)
        progressDao.insertProgress(progress)

        // When
        progressDao.updateLongestStreakIfGreater(userId, 5)

        // Then
        val result = progressDao.getProgressForUser(userId).first()
        assertEquals(7, result.longestStreak)
    }

    @Test
    fun updateCurrentStreak_updatesCurrentStreak() = coroutineRule.runTest {
        // Given
        val progress = createTestProgress(currentStreak = 3)
        progressDao.insertProgress(progress)

        // When
        progressDao.updateCurrentStreak(userId, 4)

        // Then
        val result = progressDao.getProgressForUser(userId).first()
        assertEquals(4, result.currentStreak)
    }

    // Función auxiliar para crear un progreso de prueba
    private fun createTestProgress(
        userId: Long = 0,
        totalHabitsCompleted: Int = 0,
        totalXp: Int = 0,
        currentStreak: Int = 0,
        longestStreak: Int = 0
    ): ProgressEntity {
        return TestDataFactory.createProgressEntity(
            userId = this.userId,
            totalHabitsCompleted = totalHabitsCompleted,
            totalXp = totalXp,
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }
}