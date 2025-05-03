package com.alejandro.habitjourney.features.reward.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.reward.data.entity.RewardEntity
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var rewardDao: RewardDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        rewardDao = database.rewardDao()
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
    fun insertReward_returnsId() = coroutineRule.runTest {
        // Given
        val reward = createTestReward()

        // When
        val rewardId = rewardDao.insert(reward)

        // Then
        assertTrue(rewardId > 0)
    }

    @Test
    fun updateReward_updatesCorrectReward() = coroutineRule.runTest {
        // Given
        val reward = createTestReward()
        val rewardId = rewardDao.insert(reward)
        val updatedReward = reward.copy(
            id = rewardId,
            name = "Updated Reward",
            description = "Updated description"
        )

        // When
        rewardDao.update(updatedReward)

        // Then
        val result = rewardDao.getRewards(userId, false, true).first().first { it.id == rewardId }
        assertEquals("Updated Reward", result.name)
        assertEquals("Updated description", result.description)
    }

    @Test
    fun getUnclaimed_returnsOnlyUnclaimedRewards() = coroutineRule.runTest {
        // Given
        // Insert unclaimed rewards
        val unclaimedRewards = listOf(
            createTestReward(name = "Unclaimed Reward 1"),
            createTestReward(name = "Unclaimed Reward 2"),
            createTestReward(name = "Unclaimed Reward 3")
        )
        unclaimedRewards.forEach { rewardDao.insert(it) }

        // Insert claimed reward
        val claimedReward = createTestReward(name = "Claimed Reward", isClaimed = true)
        rewardDao.insert(claimedReward)

        // When
        val result = rewardDao.getUnclaimed(userId).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { reward ->
            assertFalse(reward.isClaimed)
        }
    }

    @Test
    fun markAsClaimed_marksRewardAsClaimed() = coroutineRule.runTest {
        // Given
        val reward = createTestReward()
        val rewardId = rewardDao.insert(reward)

        // When
        rewardDao.markAsClaimed(rewardId)

        // Then
        val unclaimedRewards = rewardDao.getUnclaimed(userId).first()
        assertTrue(unclaimedRewards.none { it.id == rewardId })

        val claimedRewards = rewardDao.getRewards(userId, true).first()
        assertTrue(claimedRewards.any { it.id == rewardId })
    }

    @Test
    fun getRewards_withShowAll_returnsAllRewards() = coroutineRule.runTest {
        // Given
        // Insert unclaimed rewards
        val unclaimedRewards = listOf(
            createTestReward(name = "Unclaimed Reward 1"),
            createTestReward(name = "Unclaimed Reward 2")
        )
        unclaimedRewards.forEach { rewardDao.insert(it) }

        // Insert claimed rewards
        val claimedRewards = listOf(
            createTestReward(name = "Claimed Reward 1", isClaimed = true),
            createTestReward(name = "Claimed Reward 2", isClaimed = true)
        )
        claimedRewards.forEach { rewardDao.insert(it) }

        // When
        val result = rewardDao.getRewards(userId, false, true).first()

        // Then
        assertEquals(4, result.size)
    }

    @Test
    fun getRewards_withIsClaimed_returnsOnlyClaimedRewards() = coroutineRule.runTest {
        // Given
        // Insert unclaimed rewards
        val unclaimedRewards = listOf(
            createTestReward(name = "Unclaimed Reward 1"),
            createTestReward(name = "Unclaimed Reward 2")
        )
        unclaimedRewards.forEach { rewardDao.insert(it) }

        // Insert claimed rewards
        val claimedRewards = listOf(
            createTestReward(name = "Claimed Reward 1", isClaimed = true),
            createTestReward(name = "Claimed Reward 2", isClaimed = true),
            createTestReward(name = "Claimed Reward 3", isClaimed = true)
        )
        claimedRewards.forEach { rewardDao.insert(it) }

        // When
        val result = rewardDao.getRewards(userId, true).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { reward ->
            assertTrue(reward.isClaimed)
        }
    }

    // Función auxiliar para crear una recompensa de prueba usando TestDataFactory
    private fun createTestReward(
        name: String = "Test Reward",
        description: String = "Test description",
        isClaimed: Boolean = false
    ): RewardEntity {
        return TestDataFactory.createRewardEntity(
            userId = userId,
            name = name,
            description = description,
            isClaimed = isClaimed
        )
    }
}