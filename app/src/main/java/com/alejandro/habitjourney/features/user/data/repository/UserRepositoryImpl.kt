package com.alejandro.habitjourney.features.user.data.repository

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.data.remote.network.safeApiCall
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity
import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import com.alejandro.habitjourney.features.user.data.remote.api.AuthApi
import com.alejandro.habitjourney.features.user.data.remote.dto.request.LoginRequest
import com.alejandro.habitjourney.features.user.data.remote.dto.request.PasswordChangeDTO
import com.alejandro.habitjourney.features.user.data.remote.dto.request.RegisterRequest
import com.alejandro.habitjourney.features.user.data.remote.dto.UserDTO
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta del [UserRepository] que gestiona las operaciones de usuario
 * combinando fuentes de datos locales (Room, DataStore) y remotas (API REST).
 *
 * Se encarga de la lógica de registro, inicio de sesión, actualización, cambio de contraseña,
 * eliminación de usuario y cierre de sesión, así como de la persistencia local de los datos
 * del usuario y su estado de autenticación.
 *
 * @property context El contexto de la aplicación, inyectado por Hilt.
 * @property authApi La interfaz de la API de autenticación para interactuar con el backend.
 * @property userDao El DAO de usuario para interactuar con la base de datos local (Room).
 * @property userPreferences El gestor de preferencias de usuario (DataStore) para tokens y User ID.
 * @property errorHandler El manejador de errores para procesar excepciones de red y otras.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val errorHandler: ErrorHandler
) : UserRepository {

    /**
     * Registra un nuevo usuario en el backend y lo guarda localmente.
     *
     * @param name El nombre del usuario.
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación, conteniendo el [User] si es exitoso.
     */
    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.registerUser(RegisterRequest(name, email, password))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                try {
                    val registerResponse = apiResponse.data
                    val user = mapUserDtoToDomain(registerResponse.user)
                    val userEntity = mapUserDtoToEntity(registerResponse.user)
                    userDao.insertUser(userEntity)
                    userPreferences.saveUserId(user.id)
                    NetworkResponse.Success(user)
                } catch (e: Exception) {
                    NetworkResponse.Error(
                        Exception(context.getString(R.string.error_saving_user_locally))
                    )
                }
            }
            is NetworkResponse.Error -> {
                NetworkResponse.Error(apiResponse.exception)
            }
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Inicia sesión con un usuario existente, validando las credenciales en el backend.
     * Guarda el token de autenticación y el ID de usuario en las preferencias.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] con el [User] y el token si el inicio de sesión es exitoso.
     */
    override suspend fun login(
        email: String,
        password: String
    ): NetworkResponse<Pair<User, String>> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.loginUser(LoginRequest(email, password))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val loginResponse = apiResponse.data
                val userExists = userDao.userExists(loginResponse.user.id)
                if (!userExists) {
                    try {
                        val userEntity = mapUserDtoToEntity(loginResponse.user)
                        userDao.insertUser(userEntity)
                    } catch (e: Exception) {
                        return NetworkResponse.Error(
                            Exception(context.getString(R.string.error_recreating_local_user))
                        )
                    }
                }

                userPreferences.saveAuthToken(loginResponse.token)
                userPreferences.saveUserId(loginResponse.user.id)

                val user = mapUserDtoToDomain(loginResponse.user)
                NetworkResponse.Success(Pair(user, loginResponse.token))
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Obtiene los datos del usuario actual desde el backend.
     *
     * @return Un [NetworkResponse] con el [User] actual si la operación es exitosa.
     */
    override suspend fun getCurrentUser(): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.getCurrentUser()
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val userResponse = apiResponse.data
                val user = mapUserDtoToDomain(userResponse.user)
                NetworkResponse.Success(user)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Actualiza la información (nombre y correo electrónico) del usuario en el backend y en la base de datos local.
     *
     * @param name El nuevo nombre del usuario.
     * @param email El nuevo correo electrónico del usuario.
     * @return Un [NetworkResponse] con el [User] actualizado si la operación es exitosa.
     */
    override suspend fun updateUser(
        name: String,
        email: String
    ): NetworkResponse<User> {

        val currentUserId = userPreferences.getUserId()
            ?: return NetworkResponse.Error(
                Exception(context.getString(R.string.error_no_active_session))
            )

        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.updateUser(UserDTO(id = currentUserId, name = name, email = email))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val userResponse = apiResponse.data
                val user = mapUserDtoToDomain(userResponse.user)

                val rowsUpdated = userDao.updateUserInfo(
                    userId = user.id,
                    name = user.name,
                    email = user.email
                )
                if (rowsUpdated == 0) {
                    return NetworkResponse.Error(
                        Exception(context.getString(R.string.error_user_not_found_locally))
                    )
                }

                NetworkResponse.Success(user)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Cambia la contraseña del usuario en el backend.
     *
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña para el usuario.
     * @return Un [NetworkResponse] con un mensaje de éxito si la operación es exitosa.
     */
    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): NetworkResponse<String> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.changePassword(PasswordChangeDTO(currentPassword, newPassword))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                NetworkResponse.Success(apiResponse.data.message)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Elimina la cuenta del usuario de forma permanente.
     * Borra la cuenta en el backend, elimina los datos del usuario de la base de datos local
     * (lo que, debido a las restricciones de clave externa, debería eliminar datos relacionados como hábitos y tareas)
     * y limpia las preferencias de sesión.
     *
     * @return Un [NetworkResponse] con un mensaje de éxito si la operación es exitosa.
     */
    override suspend fun deleteUser(): NetworkResponse<String> {
        val userId = userPreferences.getUserId()
            ?: return NetworkResponse.Error(
                Exception(context.getString(R.string.error_no_active_session))
            )

        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.deleteUser()
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                userPreferences.clear()
                userDao.deleteUser(userId)
                NetworkResponse.Success(apiResponse.data.message)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(
                IllegalStateException(context.getString(R.string.error_unexpected_loading_state))
            )
        }
    }

    /**
     * Cierra la sesión actual del usuario.
     * Limpia las credenciales de sesión y el ID de usuario de las preferencias locales.
     * Los datos de usuario en la base de datos local permanecen intactos.
     *
     * @return Un [NetworkResponse.Success] sin datos si el cierre de sesión es exitoso.
     */
    override suspend fun logout(): NetworkResponse<Unit> {
        userPreferences.clear()
        return NetworkResponse.Success(Unit)
    }

    /**
     * Observa el usuario local actual basándose en el ID de usuario almacenado en las preferencias.
     *
     * @return Un [Flow] que emite el [User] local o `null` si no hay usuario activo o no se encuentra.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLocalUser(): Flow<User?> {
        return userPreferences.userIdFlow.flatMapLatest { userId ->
            if (userId != null) {
                userDao.getUserById(userId).map { entity ->
                    entity?.let { mapEntityToDomain(it) }
                }
            } else {
                flowOf(null)
            }
        }
    }

    /**
     * Verifica si hay una sesión de usuario activa.
     * Una sesión se considera activa si hay un token de autenticación presente en las preferencias.
     *
     * @return Un [Flow] que emite `true` si hay una sesión activa, `false` en caso contrario.
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return userPreferences.authTokenFlow.map { token ->
            !token.isNullOrEmpty()
        }
    }

    /**
     * Mapea un objeto [UserDTO] (Data Transfer Object del backend) a un objeto [User] de dominio.
     *
     * @param dto El [UserDTO] a mapear.
     * @return El objeto [User] de dominio correspondiente.
     */
    private fun mapUserDtoToDomain(dto: UserDTO) = User(
        id = dto.id,
        name = dto.name,
        email = dto.email
    )

    /**
     * Mapea un objeto [UserDTO] (Data Transfer Object del backend) a una [UserEntity] de Room.
     *
     * @param dto El [UserDTO] a mapear.
     * @return La [UserEntity] correspondiente.
     */
    private fun mapUserDtoToEntity(dto: UserDTO) = UserEntity(
        id = dto.id,
        name = dto.name,
        email = dto.email
    )

    /**
     * Mapea una [UserEntity] de Room a un objeto [User] de dominio.
     *
     * @param entity La [UserEntity] a mapear.
     * @return El objeto [User] de dominio correspondiente.
     */
    private fun mapEntityToDomain(entity: UserEntity) = User(
        id = entity.id,
        name = entity.name,
        email = entity.email
    )
}