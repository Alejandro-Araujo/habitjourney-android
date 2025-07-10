package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para el inicio de sesión de un usuario.
 *
 * Este caso de uso encapsula la lógica de negocio para validar las credenciales
 * (correo electrónico y contraseña) y delegar la operación de inicio de sesión al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja la autenticación.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas (mensajes de error).
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) {
    /**
     * Ejecuta la operación de inicio de sesión.
     *
     * Realiza validaciones básicas para asegurar que el correo electrónico y la contraseña
     * no estén vacíos antes de intentar el inicio de sesión a través del repositorio.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     */
    suspend operator fun invoke(email: String, password: String): NetworkResponse<User> {
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_email_empty)))
        }
        if (password.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_password_empty)))
        }
        return userRepository.login(email, password)
    }
}