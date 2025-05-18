package com.alejandro.habitjourney.features.achievement.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.core.util.TestDataFactory.TEST_TIMESTAMP
import com.alejandro.habitjourney.features.achievement.data.entity.AchievementDefinitionEntity
import com.alejandro.habitjourney.features.achievement.data.entity.UserAchievementEntity
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserAchievementDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var userAchievementDao: UserAchievementDao
    private lateinit var achievementDefinitionDao: AchievementDefinitionDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0
    private var achievementId1: Long = 0
    private var achievementId2: Long = 0

    @Before
    fun setupDatabase() = coroutineRule.runTest {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()
        userAchievementDao = database.userAchievementDao()
        achievementDefinitionDao = database.achievementDefinitionDao()
        userDao = database.userDao()

        // Crear un usuario para las pruebas
        val user = TestDataFactory.createUserEntity(
            name = "testuser",
            email = "test@example.com"
        )
        userId = userDao.insertUser(user)

        // Crear definiciones de logros para las pruebas
        val definition1 = createTestAchievementDefinition("CODE_1", "Achievement 1")
        val definition2 = createTestAchievementDefinition("CODE_2", "Achievement 2")
        achievementId1 = achievementDefinitionDao.insertDefinition(definition1)
        achievementId2 = achievementDefinitionDao.insertDefinition(definition2)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun assignAchievement_returnsIdOnSuccessfulInsert() = coroutineRule.runTest {
        // Given
        val userAchievement = createUserAchievement(userId, achievementId1)

        // When
        val result = userAchievementDao.assignAchievement(userAchievement)

        // Then
        assertTrue(result > 0)
    }

    @Test
    fun assignAchievement_returnsNegativeOneOnConflict() = coroutineRule.runTest {
        // Given
        val userAchievement = createUserAchievement(userId, achievementId1)
        userAchievementDao.assignAchievement(userAchievement)

        // When - Intentar asignar el mismo logro de nuevo
        val result = userAchievementDao.assignAchievement(userAchievement)

        // Then - Debería devolver -1 indicando que no se insertó debido al conflicto
        assertEquals(-1, result)
    }

    @Test
    fun getAchievementsForUser_returnsCorrectAchievements() = coroutineRule.runTest {
        // Given
        val userAchievement1 = createUserAchievement(userId, achievementId1)
        val userAchievement2 = createUserAchievement(userId, achievementId2)
        userAchievementDao.assignAchievement(userAchievement1)
        userAchievementDao.assignAchievement(userAchievement2)

        // When
        val result = userAchievementDao.getAchievementsForUser(userId).first()

        // Then
        assertEquals(2, result.size)
        val achievementIds = result.map { it.achievementDefinitionId }
        assertTrue(achievementId1 in achievementIds)
        assertTrue(achievementId2 in achievementIds)
    }

    @Test
    fun isAchievementUnlocked_returnsEntityWhenUnlocked() = coroutineRule.runTest {
        // Given
        val userAchievement = createUserAchievement(userId, achievementId1)
        userAchievementDao.assignAchievement(userAchievement)

        // When
        val result = userAchievementDao.isAchievementUnlocked(userId, achievementId1)

        // Then
        assertNotNull(result)
        assertEquals(userId, result?.userId)
        assertEquals(achievementId1, result?.achievementDefinitionId)
    }

    @Test
    fun isAchievementUnlocked_returnsNullWhenNotUnlocked() = coroutineRule.runTest {
        // When - No hemos desbloqueado el logro
        val result = userAchievementDao.isAchievementUnlocked(userId, achievementId1)

        // Then
        assertNull(result)
    }

    @Test
    fun getByUser_returnsAllUserAchievements() = coroutineRule.runTest {
        // Given
        val userAchievement1 = createUserAchievement(userId, achievementId1)
        val userAchievement2 = createUserAchievement(userId, achievementId2)
        userAchievementDao.assignAchievement(userAchievement1)
        userAchievementDao.assignAchievement(userAchievement2)

        // When
        val result = userAchievementDao.getByUser(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun getAchievementCompletionPercentage_calculatesCorrectPercentage() = coroutineRule.runTest {
        // Given - 2 definiciones de logros pero solo 1 desbloqueado
        val userAchievement = createUserAchievement(userId, achievementId1)
        userAchievementDao.assignAchievement(userAchievement)

        // When
        val percentage = userAchievementDao.getAchievementCompletionPercentage(userId)

        // Then
        assertEquals(50.0f, percentage, 0.001f)
    }

    @Test
    fun getRecentAchievements_returnsLimitedAndOrderedAchievements() = coroutineRule.runTest {
        // Given
        val userAchievement1 = createUserAchievement(userId, achievementId1, unlockedAt = 1000L)
        val userAchievement2 = createUserAchievement(userId, achievementId2, unlockedAt = 2000L)
        userAchievementDao.assignAchievement(userAchievement1)
        userAchievementDao.assignAchievement(userAchievement2)

        // When - Solicitar solo 1 logro reciente
        val result = userAchievementDao.getRecentAchievements(userId, 1).first()

        // Then - Debería devolver el más reciente (el segundo)
        assertEquals(1, result.size)
        assertEquals(achievementId2, result[0].achievementDefinitionId)
    }

    // Función auxiliar para crear una definición de logro de prueba
    private fun createTestAchievementDefinition(
        code: String,
        name: String,
        description: String = "Test description",
    ): AchievementDefinitionEntity {
        return TestDataFactory.createAchievementDefinitionEntity(
            id = 0,
            code = code,
            name = name,
            description = description,
        )
    }

    // Función auxiliar para crear un UserAchievementEntity
    private fun createUserAchievement(
        userId: Long,
        achievementId: Long,
        unlockedAt: Long = TEST_TIMESTAMP
    ): UserAchievementEntity {
        return TestDataFactory.createUserAchievementEntity(
            userId = userId,
            achievementDefinitionId = achievementId,
            unlockedAt = unlockedAt
        )
    }
}