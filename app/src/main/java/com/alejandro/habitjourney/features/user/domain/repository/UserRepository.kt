package com.alejandro.habitjourney.features.user.domain.repository

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    suspend fun login(email: String, password: String): NetworkResponse<Pair<User, String>>

    suspend fun register(name: String, email: String, password: String): NetworkResponse<User>

    suspend fun getCurrentUser(): NetworkResponse<User>

    suspend fun updateUser(name: String, email: String): NetworkResponse<User>

    suspend fun changePassword(currentPassword: String, newPassword: String): NetworkResponse<String>

    suspend fun deleteUser(): NetworkResponse<String>

    suspend fun logout(): NetworkResponse<Unit>

    fun getLocalUser(): Flow<User?>

    fun isLoggedIn(): Flow<Boolean>
}