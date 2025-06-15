package com.alejandro.habitjourney.features.user.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de usuario en la base de datos local.
 *
 * Gestiona el almacenamiento y recuperación del perfil del usuario autenticado.
 * Solo debe existir un usuario activo a la vez en la aplicación.
 */
@Dao
interface UserDao {

    /**
     * Observa al usuario actualmente autenticado.
     * @return Flow que emite el usuario actual o null si no hay sesión activa
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): Flow<UserEntity?>

    /**
     * Obtiene un usuario por ID de forma síncrona.
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserByIdSync(userId: Long): UserEntity?

    /**
     * Inserta un nuevo usuario.
     * Falla si ya existe un usuario con el mismo ID.
     *
     * @param user Usuario a insertar
     * @return ID del usuario insertado
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Actualiza campos específicos del usuario.
     *
     * @param userId ID del usuario
     * @param name Nuevo nombre
     * @param email Nuevo email
     * @return Número de filas actualizadas (debe ser 1)
     */
    @Query("""
        UPDATE users
        SET name = :name, 
            email = :email,
            updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateUserInfo(
        userId: Long,
        name: String,
        email: String,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    /**
     * Elimina el usuario actual y todos sus datos asociados.
     *
     * @param userId ID del usuario a eliminar
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    /**
     * Verifica si existe un usuario con el email dado.
     * Útil para validaciones antes del registro.
     *
     * @param email Email a verificar
     * @return true si el email ya está registrado
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun isEmailRegistered(email: String): Boolean

    /**
     * Verifica si existe un usuario con el ID dado.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)")
    suspend fun userExists(userId: Long): Boolean
}
