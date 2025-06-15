package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * **Caso de uso para actualizar la información del perfil del usuario.**
 *
 * Este caso de uso se encarga de validar los datos del perfil (nombre y correo electrónico)
 * y delegar la operación de actualización al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja la actualización del perfil.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas (mensajes de error).
 */
class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider

) {
    /**
     * Ejecuta la operación de actualización del perfil del usuario.
     *
     * Realiza validaciones básicas para asegurar que el nombre y el correo electrónico
     * no estén vacíos antes de intentar la actualización a través del repositorio.
     *
     * @param name El nuevo nombre del usuario.
     * @param email El nuevo correo electrónico del usuario.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     * Si es exitoso, contendrá el objeto [User] actualizado.
     */
    suspend operator fun invoke(name: String, email: String): NetworkResponse<User> {
        if (name.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_name_empty)))
        }
        if (email.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_email_empty)))
        }
        return userRepository.updateUser(name, email)
    }
}