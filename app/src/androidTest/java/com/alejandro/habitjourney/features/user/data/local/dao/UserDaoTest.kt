package com.alejandro.habitjourney.features.user.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import junit.framework.TestCase.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserDaoTest {

    // Reglas de JUnit
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setupDatabase() {
        database = TestDataFactory.createInMemoryDatabase()
        userDao = database.userDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun testInsertAndGetUserById() = coroutineRule.runTest {
        // Given: creamos un usuario con un ID específico
        val user = TestDataFactory.createUserEntity(id = "1L", name = "Test User", email = "test@example.com")

        // When: insertamos el usuario
        val insertedId = userDao.insertUser(user)

        // Then: verificamos que podemos recuperarlo por su ID
        val retrievedUser = userDao.getUserById(insertedId.toString()).first()

        assertNotNull(retrievedUser)
        assertEquals(user.id, retrievedUser?.id)
        assertEquals(user.name, retrievedUser?.name)
        assertEquals(user.email, retrievedUser?.email)
    }

    @Test
    fun testUpdateUserInfo() = coroutineRule.runTest {
        // Given: un usuario insertado
        val user = TestDataFactory.createUserEntity(id = "1L", name = "Original Name", email = "original@example.com")
        val userId = userDao.insertUser(user)

        // When: actualizamos su información
        val newName = "Updated Name"
        val newEmail = "updated@test.com"
        val rowsAffected = userDao.updateUserInfo(userId.toString(), newName, newEmail)

        // Then: verificamos que la operación afectó a una fila
        assertEquals(1, rowsAffected)

        // And: verificamos que los datos se actualizaron correctamente
        val updatedUser = userDao.getUserById(userId.toString()).first()
        assertEquals(newName, updatedUser?.name)
        assertEquals(newEmail, updatedUser?.email)
        // Opcional: Verificar que updatedAt ha cambiado
        assertTrue(updatedUser!!.updatedAt > user.updatedAt)
    }

    @Test
    fun testDeleteUser() = coroutineRule.runTest {
        // Given: un usuario insertado
        val user = TestDataFactory.createUserEntity(id = "1L")
        val userId = userDao.insertUser(user)

        // Verificamos que existe antes de borrar
        assertNotNull(userDao.getUserById(userId.toString()).first())

        // When: eliminamos el usuario por su ID
        userDao.deleteUser(userId.toString())

        // Then: verificamos que el usuario ya no existe
        val retrievedUser = userDao.getUserById(userId.toString()).first()
        assertNull("El usuario debería haber sido eliminado", retrievedUser)
    }
}