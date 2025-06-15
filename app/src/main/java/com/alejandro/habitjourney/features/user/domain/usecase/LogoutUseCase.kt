package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar la sesión del usuario.
 *
 * Este caso de uso encapsula la lógica para finalizar la sesión activa del usuario,
 * delegando la operación al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja el cierre de sesión.
 */
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Ejecuta la operación de cierre de sesión.
     *
     * @return Un [NetworkResponse.Success] sin datos si el cierre de sesión es exitoso,
     * o un [NetworkResponse.Error] si ocurre un problema.
     */
    suspend operator fun invoke(): NetworkResponse<Unit> {
        return userRepository.logout()
    }
}