package com.alejandro.habitjourney.features.user.domain.usecase


import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String): NetworkResponse<User> {
        if (name.isBlank()) {
            return NetworkResponse.Error(Exception("El nombre no puede estar vacío"))
        }
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception("El email no puede estar vacío"))
        }
        return userRepository.updateUser(name, email)
    }
}