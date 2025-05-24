package com.alejandro.habitjourney.features.user.data.repository

import android.content.Context
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context, // Inyectamos el Context
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val errorHandler: ErrorHandler
) : UserRepository {

    // Implementación de los métodos definidos en UserRepository

    override suspend fun login(email: String, password: String): NetworkResponse<Pair<User, String>> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.loginUser(LoginRequest(email, password)) // Corregido a loginUser
        }

        // Usamos 'when' para manejar la respuesta y realizar operaciones suspend
        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // LoginResponse
                // Guardar token JWT en DataStore (operación suspend)
                userPreferences.saveAuthToken(responseBody.token)

                // Mapear UserDTO (remoto) a User (dominio) y UserEntity (local)
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)

                // Guardar UserEntity en Room (operación suspend)
                userDao.insertUser(userEntity)

                // Retornar el resultado exitoso con el par (User, Token)
                NetworkResponse.Success(Pair(user, responseBody.token))
            }
            is NetworkResponse.Error -> {
                // Propagar el error
                NetworkResponse.Error(apiResponse.exception)
            }
            is NetworkResponse.Loading -> {
                // Si NetworkResponse tiene estado Loading, manejarlo.
                // Asumiendo que safeApiCall no emite Loading en este contexto directo.
                // Si lo hiciera, la lógica de NetworkResponse debería ser diferente (ej: Flow).
                // Para esta estructura, asumimos que Loading no llega aquí.
                NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
            }
        }
    }

    override suspend fun register(name: String, email: String, password: String): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.registerUser(RegisterRequest(name, email, password)) // Corregido a registerUser
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // RegisterResponse
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity) // Operación suspend
                NetworkResponse.Success(user)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun getCurrentUser(): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.getCurrentUser()
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // UserResponseDTO
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity) // Operación suspend
                NetworkResponse.Success(user)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun updateUser(name: String, email: String): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            val userDtoForUpdate = UserDTO(id = 0, name = name, email = email) // ID puede ser ignorado por el backend en /me
            authApi.updateUser(userDtoForUpdate)
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // UserResponseDTO
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity) // Operación suspend
                NetworkResponse.Success(user)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResponse<String> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.changePassword(PasswordChangeDTO(currentPassword, newPassword))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // MessageResponse
                NetworkResponse.Success(responseBody.message) // Extraemos el mensaje
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun deleteUser(): NetworkResponse<String> {
        // Intentamos eliminar en el backend primero
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.deleteUser()
        }

        // Usamos 'when' para manejar la respuesta
        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // MessageResponse
                // Si la eliminación en el backend es exitosa, limpiamos los datos locales (operaciones suspend)
                userPreferences.clear()
                userDao.deleteAllUsers()
                // Retornamos el mensaje de éxito del backend
                NetworkResponse.Success(responseBody.message)
            }
            is NetworkResponse.Error -> {
                // Propagar el error. Considerar si limpiar datos locales en ciertos errores (ej: 401 Unauthorized)
                NetworkResponse.Error(apiResponse.exception)
            }
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun logout(): NetworkResponse<Unit> {
        // El logout es principalmente una operación local en esta arquitectura MVP
        // Limpiamos los datos locales (token y usuario en Room) (operaciones suspend)
        userPreferences.clear()
        userDao.deleteAllUsers()
        // Retornamos éxito local.
        return NetworkResponse.Success(Unit)
    }

    override fun getLocalUser(): Flow<User?> {
        // Obtiene el Flow de UserEntity desde Room y lo mapea a User (dominio)
        // map aquí es un operador de Flow, no el map de NetworkResponse, y puede manejar suspend lambdas si es necesario,
        // pero en este caso mapEntityToUser no es suspend.
        return userDao.getUser().map { userEntity ->
            userEntity?.let { mapEntityToUser(it) }
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        // Obtiene el Flow del token desde DataStore y verifica si no es nulo ni vacío
        return userPreferences.authTokenFlow.map { token ->
            !token.isNullOrEmpty()
        }
    }

    // Funciones de mapeo entre DTOs/Entities y Modelo de Dominio
    // Eliminamos el manejo de 'createdAt'

    private fun mapUserDtoToDomain(userDto: UserDTO): User {
        return User(
            id = userDto.id,
            name = userDto.name,
            email = userDto.email
            // No mapeamos 'createdAt'
        )
    }

    private fun mapUserToEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            name = user.name,
            email = user.email
            // No mapeamos 'createdAt'
        )
    }

    private fun mapEntityToUser(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            name = userEntity.name,
            email = userEntity.email
            // No mapeamos 'createdAt'
        )
    }
}