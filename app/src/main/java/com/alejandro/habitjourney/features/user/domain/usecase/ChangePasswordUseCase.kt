package com.alejandro.habitjourney.features.user.domain.usecase


import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): NetworkResponse<String> {
        if (currentPassword.isBlank()) {
            return NetworkResponse.Error(Exception("La contraseña actual no puede estar vacía"))
        }
        if (newPassword.isBlank()) {
            return NetworkResponse.Error(Exception("La nueva contraseña no puede estar vacía"))
        }
        if (newPassword.length < 6) {
            return NetworkResponse.Error(Exception("La nueva contraseña debe tener al menos 6 caracteres"))
        }
        return userRepository.changePassword(currentPassword, newPassword)
    }
}