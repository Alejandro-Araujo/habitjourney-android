package com.alejandro.habitjourney.features.user.domain.usecase


import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

/**
 * Caso de uso para reautenticar al usuario con credenciales recientes.
 * Necesario para operaciones sensibles como cambio de contraseña, email o eliminación de cuenta.
 */
class ReauthenticateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Reautentica al usuario actual con la credencial proporcionada.
     *
     * @param credential La credencial de autenticación (EmailAuthProvider.getCredential, GoogleAuthProvider.getCredential, etc.)
     * obtenida del último inicio de sesión del usuario.
     * @return Un [NetworkResponse] indicando el resultado de la operación.
     */
    suspend operator fun invoke(credential: AuthCredential): NetworkResponse<Unit> {
        return userRepository.reauthenticateUser(credential)
    }
}