package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * **Caso de uso para la operación de inicio de sesión o registro con Google.**
 *
 * Este Use Case se encarga de usar el ID Token de Google (que ya ha sido obtenido
 * por la capa de presentación a través de Credential Manager) para autenticar al usuario
 * en el sistema (ej. Firebase).
 *
 * @property userRepository El repositorio para operaciones de usuario.
 */
class GoogleSignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    /**
     * Autentica al usuario en el repositorio utilizando el ID Token de Google proporcionado.
     *
     * @param googleIdToken El ID Token de Google obtenido de Google Credential Manager.
     * @return [NetworkResponse.Success] con el [User] si la autenticación es exitosa,
     * o [NetworkResponse.Error] si falla.
     */
    suspend operator fun invoke(googleIdToken: String): NetworkResponse<User> {
        return userRepository.signInWithGoogle(googleIdToken)
    }
}