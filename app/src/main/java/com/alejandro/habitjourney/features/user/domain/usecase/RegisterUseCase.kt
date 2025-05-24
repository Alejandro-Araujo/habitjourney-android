package com.alejandro.habitjourney.features.user.domain.usecase


import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): NetworkResponse<User> {
        if (name.isBlank()) {
            return NetworkResponse.Error(Exception("El nombre no puede estar vacío"))
        }
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception("El email no puede estar vacío"))
        }
        if (password.isBlank()) {
            return NetworkResponse.Error(Exception("La contraseña no puede estar vacía"))
        }

        // Validación simple de contraseña
        if (password.length < 6) {
            return NetworkResponse.Error(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        return userRepository.register(name, email, password)
    }
}