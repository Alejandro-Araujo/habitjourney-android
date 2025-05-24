package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResponse<Pair<User, String>> {
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception("El email no puede estar vacío"))
        }
        if (password.isBlank()) {
            return NetworkResponse.Error(Exception("La contraseña no puede estar vacía"))
        }
        return userRepository.login(email, password)
    }
}