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
        // Given: Un usuario para insertar
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )

        // When: Insertamos el usuario
        val userId = userDao.insertUser(user)

        // Then: Verificamos que podemos recuperar el usuario por ID
        val retrievedUser = userDao.getUserById(userId).first()

        // Assert: Verificamos que el usuario recuperado coincide con el insertado
        assertNotNull(retrievedUser)
        assertEquals(userId, retrievedUser.id)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.name, retrievedUser.name)
        assertEquals(user.passwordHash, retrievedUser.passwordHash)
    }

    @Test
    fun getUserByEmail() = coroutineRule.runTest {
        // Given: Un usuario insertado en la base de datos
        val email = "email@test.com"
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = email
        )
        userDao.insertUser(user)

        // When: Buscamos al usuario por email
        val found = userDao.getUserByEmail(email)

        // Then: Verificamos que encontramos al usuario correcto
        assertNotNull("Usuario debería ser encontrado", found)
        assertEquals(email, found?.email)
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
        val updatedUser = userDao.getUserById(userId).first()
        assertEquals(newName, updatedUser.name)
        assertEquals(newEmail, updatedUser.email)
    }

    @Test
    fun changePassword() = coroutineRule.runTest {
        // Given: Un usuario con contraseña conocida
        val initialHash = "initialHash"
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com",
            passwordHash = initialHash
        )
        val userId = userDao.insertUser(user)

        // When: Cambiamos la contraseña
        val newHash = "newHash"
        val rowsAffected = userDao.changePassword(userId, initialHash, newHash)

        // Then: Verificamos que se cambió correctamente
        assertEquals(1, rowsAffected)  // Debe afectar a 1 fila

        // And: Verificamos que la contraseña es la nueva
        val updatedUser = userDao.getUserById(userId).first()
        assertEquals(newHash, updatedUser.passwordHash)
    }

    @Test
    fun changePasswordWithWrongCurrentHash() = coroutineRule.runTest {
        // Given: Un usuario con contraseña conocida
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user)

        // When: Intentamos cambiar la contraseña con un hash actual incorrecto
        val wrongHash = "wrongHash"
        val newHash = "newHash"
        val rowsAffected = userDao.changePassword(userId, wrongHash, newHash)

        // Then: No se debería cambiar la contraseña
        assertEquals(0, rowsAffected)  // No debe afectar a ninguna fila

        // And: Verificamos que la contraseña sigue siendo la original
        val updatedUser = userDao.getUserById(userId).first()
        assertEquals(user.passwordHash, updatedUser.passwordHash)
    }

    @Test
    fun verifyCredentials() = coroutineRule.runTest {
        // Given: Un usuario con credenciales conocidas
        val email = "verify@test.com"
        val passwordHash = "verifyHash"
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = email,
            passwordHash = passwordHash
        )
        userDao.insertUser(user)

        // When/Then: Verificamos credenciales correctas
        val correctVerification = userDao.verifyCredentials(email, passwordHash)
        assertTrue("Las credenciales correctas deberían verificarse", correctVerification)

        // When/Then: Verificamos credenciales incorrectas
        val wrongVerification = userDao.verifyCredentials(email, "wrongHash")
        assertFalse("Las credenciales incorrectas no deberían verificarse", wrongVerification)
    }

    @Test
    fun deleteUser() = coroutineRule.runTest {
        // Given: Un usuario insertado
        val user = TestDataFactory.createUserEntity(
            name = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user)

        // Get the complete user entity with the generated ID
        val insertedUser = userDao.getUserById(userId).first()

        // When: Eliminamos al usuario
        userDao.delete(insertedUser)

        // Then: Verificamos que el usuario ya no existe
        val found = userDao.getUserByEmail(user.email)
        assertNull("El usuario debería ser eliminado", found)
    }
}