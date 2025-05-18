package com.alejandro.habitjourney.features.user.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Insertar usuario
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    // Update name and / or email
    @Query("""
        UPDATE users 
        SET name = :newName, email = :newEmail
        WHERE id = :userId
    """)
    suspend fun updateUserInfo(
        userId: Long,
        newName: String,
        newEmail: String
    ):Int

    // Change password (secure)
    @Query("""
        UPDATE users 
        SET password_hash = :newHash 
        WHERE id = :userId 
        AND password_hash = :currentHash
    """)
    suspend fun changePassword(
        userId: Long,
        currentHash: String,
        newHash: String
    ): Int  // Returns affected rows

    // Obtener usuario por email (para login)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Obtener usuario por ID
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): Flow<UserEntity>

    // Eliminar usuario
    @Delete
    suspend fun delete(user: UserEntity)

    // Verificar credenciales sin obtener toda la entidad
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM users 
            WHERE email = :email 
            AND password_hash = :passwordHash
        )
    """)
    suspend fun verifyCredentials(email: String, passwordHash: String): Boolean
}

