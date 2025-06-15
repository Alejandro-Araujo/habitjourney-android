package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * **Caso de uso para el registro de nuevos usuarios.**
 *
 * Este caso de uso encapsula la lógica de negocio para validar los datos
 * (nombre, correo electrónico y contraseña) y delegar la operación de registro al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja la lógica de registro.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas (mensajes de error).
 */
class RegisterUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) {
    /**
     * Ejecuta la operación de registro de un nuevo usuario.
     *
     * Realiza validaciones básicas para asegurar que el nombre, el correo electrónico y la contraseña
     * no estén vacíos, y que la contraseña cumpla con una longitud mínima, antes de intentar
     * el registro a través del repositorio.
     *
     * @param name El nombre del nuevo usuario.
     * @param email El correo electrónico del nuevo usuario.
     * @param password La contraseña del nuevo usuario.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     * Si es exitoso, contendrá el objeto [User] del usuario registrado.
     */
    suspend operator fun invoke(name: String, email: String, password: String): NetworkResponse<User> {
        if (name.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_name_empty)))
        }
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_email_empty)))
        }
        if (password.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_password_empty)))
        }

        if (password.length < 6) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_password_min_length)))
        }

        return userRepository.register(name, email, password)
    }
}