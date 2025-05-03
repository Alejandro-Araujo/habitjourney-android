package com.alejandro.habitjourney.features.achievement.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.achievement.data.entity.AchievementDefinitionEntity
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AchievementDefinitionDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var achievementDefinitionDao: AchievementDefinitionDao

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = com.alejandro.habitjourney.core.util.TestDataFactory.createInMemoryDatabase()
        achievementDefinitionDao = database.achievementDefinitionDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertDefinition_returnsId() = coroutineRule.runTest {
        // Given
        val definition = createTestAchievementDefinition()

        // When
        val definitionId = achievementDefinitionDao.insertDefinition(definition)

        // Then
        assertTrue(definitionId > 0)
    }

    @Test
    fun updateDefinition_updatesCorrectDefinition() = coroutineRule.runTest {
        // Given
        val definition = createTestAchievementDefinition()
        val definitionId = achievementDefinitionDao.insertDefinition(definition)

        // When
        val updatedDefinition = definition.copy(
            id = definitionId,
            name = "Updated Achievement",
            description = "Updated description"
        )
        achievementDefinitionDao.updateDefinition(updatedDefinition)

        // Then
        val result = achievementDefinitionDao.getDefinitionByCode(definition.code)
        assertEquals("Updated Achievement", result?.name)
        assertEquals("Updated description", result?.description)
    }

    @Test
    fun getAllDefinitions_returnsAllInsertedDefinitions() = coroutineRule.runTest {
        // Given
        val definitions = listOf(
            createTestAchievementDefinition(code = "TEST_CODE_1", name = "Test Achievement 1"),
            createTestAchievementDefinition(code = "TEST_CODE_2", name = "Test Achievement 2"),
            createTestAchievementDefinition(code = "TEST_CODE_3", name = "Test Achievement 3")
        )

        // When
        definitions.forEach { achievementDefinitionDao.insertDefinition(it) }
        val result = achievementDefinitionDao.getAllDefinitions().first()

        // Then
        assertEquals(3, result.size)
        val achievementCodes = result.map { it.code }
        assertTrue("TEST_CODE_1" in achievementCodes)
        assertTrue("TEST_CODE_2" in achievementCodes)
        assertTrue("TEST_CODE_3" in achievementCodes)
    }

    @Test
    fun getDefinitionByCode_returnsCorrectDefinition() = coroutineRule.runTest {
        // Given
        val definition = createTestAchievementDefinition(code = "SPECIFIC_CODE")
        achievementDefinitionDao.insertDefinition(definition)

        // When
        val result = achievementDefinitionDao.getDefinitionByCode("SPECIFIC_CODE")

        // Then
        assertNotNull(result)
        assertEquals("SPECIFIC_CODE", result?.code)
        assertEquals("Test Achievement", result?.name)
    }

    @Test
    fun getDefinitionByCode_returnsNullForNonExistentCode() = coroutineRule.runTest {
        // When
        val result = achievementDefinitionDao.getDefinitionByCode("NON_EXISTENT_CODE")

        // Then
        assertNull(result)
    }

    @Test
    fun deleteDefinitionByCode_deletesCorrectDefinition() = coroutineRule.runTest {
        // Given
        val definitions = listOf(
            createTestAchievementDefinition(code = "CODE_1"),
            createTestAchievementDefinition(code = "CODE_2")
        )
        definitions.forEach { achievementDefinitionDao.insertDefinition(it) }

        // When
        val deletedCount = achievementDefinitionDao.deleteDefinitionByCode("CODE_1")
        val allDefinitions = achievementDefinitionDao.getAllDefinitions().first()

        // Then
        assertEquals(1, deletedCount)
        assertEquals(1, allDefinitions.size)
        assertEquals("CODE_2", allDefinitions[0].code)
    }

    // Función auxiliar para crear una definición de logro de prueba
    private fun createTestAchievementDefinition(
        id: Long = 0,
        code: String = "TEST_CODE",
        name: String = "Test Achievement",
        description: String = "Test description",
    ): AchievementDefinitionEntity {
        return TestDataFactory.createAchievementDefinitionEntity(
            id = id,
            code = code,
            name = name,
            description = description,
        )
    }
}