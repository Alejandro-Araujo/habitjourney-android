package com.alejandro.habitjourney.features.user.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
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

    private lateinit var database: AppDatabase // Instancia de la base de datos en memoria
    private lateinit var userDao: UserDao // Instancia del DAO a testear

    @Before
    fun setupDatabase() {
        // Configura una base de datos en memoria para los tests
        database = TestDataFactory.createInMemoryDatabase()

        // Obtiene la instancia del DAO desde la base de datos
        userDao = database.userDao()
    }

    @After
    fun closeDatabase() {
        // Cierra la base de datos en memoria
        database.close()
    }

    @Test
    fun testInsertAndGetUser() = coroutineRule.runTest {
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user) // Insertamos el usuario

        // Verificamos que podemos recuperar el usuario único
        val retrievedUser = userDao.getUser().first() // CAMBIO: Usar getUser() en lugar de getUserById()

        assertNotNull(retrievedUser)
        assertEquals(userId, retrievedUser?.id) // Usar ?.id por si es nulo
        assertEquals(user.email, retrievedUser?.email)
        assertEquals(user.name, retrievedUser?.name)
    }

    @Test
    fun updateUserInfo() = coroutineRule.runTest {
        // Given: Un usuario insertado
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user)

        // When: Actualizamos la información del usuario
        val newName = "Updated Name"
        val newEmail = "updated@test.com"
        val rowsAffected = userDao.updateUserInfo(userId, newName, newEmail)

        // Then: Verificamos que se actualizó correctamente
        assertEquals(1, rowsAffected)  // Debe afectar a 1 fila

        // And: Verificamos que los datos se actualizaron correctamente
        val updatedUser = userDao.getUser().first() // CAMBIO: Usar getUser()
        assertEquals(newName, updatedUser?.name) // Usar ?.name
        assertEquals(newEmail, updatedUser?.email) // Usar ?.email
    }

    @Test
    fun deleteUser() = coroutineRule.runTest {
        // Given: Un usuario insertado
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user)

        // When: Eliminamos todos los usuarios (en este caso, el único)
        userDao.deleteAllUsers() // CAMBIO: Usar deleteAllUsers()

        // Then: Verificamos que el usuario ya no existe
        val found = userDao.getUser().first() // CAMBIO: Usar getUser()
        assertNull("El usuario debería ser eliminado", found)
    }
}