package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar la cuenta de usuario.
 *
 * Este caso de uso encapsula la lógica para solicitar la eliminación permanente
 * de la cuenta de usuario, delegando la operación al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja la eliminación de la cuenta.
 */
class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Ejecuta la operación de eliminación de la cuenta de usuario.
     *
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     * Si es exitoso, contendrá un mensaje de confirmación de eliminación.
     */
    suspend operator fun invoke(): NetworkResponse<String> {
        return userRepository.deleteUser()
    }
}