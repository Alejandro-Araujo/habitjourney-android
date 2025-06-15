package com.alejandro.habitjourney.features.user.domain.repository

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de repositorio para la gestión de usuarios.
 *
 * Define las operaciones que la capa de dominio necesita para interactuar con los datos del usuario,
 * incluyendo autenticación, gestión del perfil y estado de la sesión, sin exponer los detalles
 * de la implementación de la capa de datos (local o remota).
 */
interface UserRepository {
    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] que contiene un [Pair] del [User] y el token de autenticación si el inicio de sesión es exitoso.
     */
    suspend fun login(email: String, password: String): NetworkResponse<Pair<User, String>>

    /**
     * Registra un nuevo usuario con los datos proporcionados.
     *
     * @param name El nombre del nuevo usuario.
     * @param email El correo electrónico del nuevo usuario.
     * @param password La contraseña del nuevo usuario.
     * @return Un [NetworkResponse] que contiene el [User] creado si el registro es exitoso.
     */
    suspend fun register(name: String, email: String, password: String): NetworkResponse<User>

    /**
     * Obtiene los datos del usuario actualmente autenticado desde la fuente de datos.
     *
     * @return Un [NetworkResponse] que contiene el [User] actual si la operación es exitosa.
     */
    suspend fun getCurrentUser(): NetworkResponse<User>

    /**
     * Actualiza la información de perfil del usuario (nombre y correo electrónico).
     *
     * @param name El nuevo nombre del usuario.
     * @param email El nuevo correo electrónico del usuario.
     * @return Un [NetworkResponse] que contiene el [User] actualizado si la operación es exitosa.
     */
    suspend fun updateUser(name: String, email: String): NetworkResponse<User>

    /**
     * Cambia la contraseña del usuario actual.
     *
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña que se desea establecer.
     * @return Un [NetworkResponse] que contiene un mensaje de éxito si la operación es exitosa.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResponse<String>

    /**
     * Elimina la cuenta del usuario de forma permanente.
     *
     * @return Un [NetworkResponse] que contiene un mensaje de éxito si la operación es exitosa.
     */
    suspend fun deleteUser(): NetworkResponse<String>

    /**
     * Cierra la sesión actual del usuario, limpiando cualquier información de sesión.
     *
     * @return Un [NetworkResponse.Success] sin datos si el cierre de sesión es exitoso.
     */
    suspend fun logout(): NetworkResponse<Unit>

    /**
     * Obtiene un [Flow] que emite el objeto [User] almacenado localmente.
     * Esto permite a los observadores reaccionar a los cambios en los datos del usuario localmente.
     *
     * @return Un [Flow] que emite el [User] local o `null` si no hay un usuario local.
     */
    fun getLocalUser(): Flow<User?>

    /**
     * Obtiene un [Flow] que indica si hay una sesión de usuario activa.
     *
     * @return Un [Flow] que emite `true` si el usuario está autenticado, `false` en caso contrario.
     */
    fun isLoggedIn(): Flow<Boolean>
}