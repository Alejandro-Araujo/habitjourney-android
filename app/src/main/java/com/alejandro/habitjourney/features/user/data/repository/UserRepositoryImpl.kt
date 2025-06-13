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
import com.alejandro.habitjourney.features.user.data.remote.dto.response.LoginResponse
import com.alejandro.habitjourney.features.user.data.remote.dto.response.RegisterResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences,
    private val errorHandler: ErrorHandler
) : UserRepository {

    // Implementación de los métodos definidos en UserRepository

    override suspend fun login(email: String, password: String): NetworkResponse<Pair<User, String>> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.loginUser(LoginRequest(email, password))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data as LoginResponse
                userPreferences.saveAuthToken(responseBody.token)
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity)

                userPreferences.saveUserId(user.id)

                NetworkResponse.Success(Pair(user, responseBody.token))
            }
            is NetworkResponse.Error -> {
                NetworkResponse.Error(apiResponse.exception)
            }
            is NetworkResponse.Loading -> {
                NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
            }
        }
    }

    override suspend fun register(name: String, email: String, password: String): NetworkResponse<User> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.registerUser(RegisterRequest(name, email, password))
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data as RegisterResponse
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity)

                userPreferences.saveUserId(user.id)
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
            val userDtoForUpdate = UserDTO(id = 0, name = name, email = email)
            authApi.updateUser(userDtoForUpdate)
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data
                val user = mapUserDtoToDomain(responseBody.user)
                val userEntity = mapUserToEntity(user)
                userDao.insertUser(userEntity)
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
                val responseBody = apiResponse.data
                NetworkResponse.Success(responseBody.message)
            }
            is NetworkResponse.Error -> NetworkResponse.Error(apiResponse.exception)
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun deleteUser(): NetworkResponse<String> {
        val apiResponse = safeApiCall(context, errorHandler) {
            authApi.deleteUser()
        }

        return when (apiResponse) {
            is NetworkResponse.Success -> {
                val responseBody = apiResponse.data // MessageResponse
                userPreferences.clear()
                userDao.deleteAllUsers()
                NetworkResponse.Success(responseBody.message)
            }
            is NetworkResponse.Error -> {
                NetworkResponse.Error(apiResponse.exception)
            }
            is NetworkResponse.Loading -> NetworkResponse.Error(IllegalStateException("Loading state not expected after safeApiCall"))
        }
    }

    override suspend fun logout(): NetworkResponse<Unit> {
        userPreferences.clear()
        return NetworkResponse.Success(Unit)
    }

    override fun getLocalUser(): Flow<User?> {
        return userDao.getUser().map { userEntity ->
            userEntity?.let { mapEntityToUser(it) }
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return userPreferences.authTokenFlow.map { token ->
            !token.isNullOrEmpty()
        }
    }

    private fun mapUserDtoToDomain(userDto: UserDTO): User {
        return User(
            id = userDto.id,
            name = userDto.name,
            email = userDto.email
        )
    }

    private fun mapUserToEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            name = user.name,
            email = user.email
        )
    }

    private fun mapEntityToUser(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            name = userEntity.name,
            email = userEntity.email
        )
    }
}