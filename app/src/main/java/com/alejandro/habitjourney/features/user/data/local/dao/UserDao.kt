package com.alejandro.habitjourney.features.user.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la entidad UserEntity.
 * Define las operaciones de base de datos local para la información del perfil del usuario.
 * NO debe contener métodos para la gestión de contraseñas, ya que la autenticación se maneja en el backend.
 */
@Dao
interface UserDao {

    /**
     * Obtiene el usuario actualmente almacenado en la base de datos local.
     * Como solo debería haber un usuario autenticado a la vez, limitamos a 1.
     * Retorna un Flow para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    /**
     * Inserta un nuevo usuario o reemplaza al usuario existente si ya hay uno con el mismo ID.
     * Esto es útil después de un login o registro exitoso para guardar/actualizar el perfil localmente.
     * @param user La entidad UserEntity a insertar/actualizar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity):Long

    /**
     * Actualiza el nombre y/o email del usuario localmente.
     * Esto podría usarse si permites editar el perfil offline o si el backend devuelve el usuario actualizado.
     * @param userId El ID del usuario a actualizar.
     * @param newName El nuevo nombre del usuario.
     * @param newEmail El nuevo email del usuario.
     * @return El número de filas afectadas (debería ser 1 si el usuario existe).
     */
    @Query("""
        UPDATE users
        SET name = :newName, email = :newEmail
        WHERE id = :userId
    """)
    suspend fun updateUserInfo(
        userId: Long,
        newName: String,
        newEmail: String
    ): Int

    /**
     * Elimina todos los usuarios de la tabla 'users'.
     * Esto se usa típicamente al cerrar la sesión del usuario.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // --- Métodos relacionados con contraseñas o autenticación local ELIMINADOS ---
    // Ya que la autenticación se gestiona en el backend.
    // - changePassword()
    // - getUserByEmail() para login local
    // - verifyCredentials()
    // - delete(user: UserEntity) - Usamos deleteAllUsers() para simplificar la limpieza del usuario único.
    // - getUserById() - getUser() es suficiente para el usuario único autenticado.
}
