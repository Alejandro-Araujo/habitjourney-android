package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.core.utils.resources.ResourceProvider
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para cambiar la contraseña del usuario.
 *
 * Este caso de uso encapsula la lógica de negocio para validar las contraseñas
 * y delegar la operación de cambio de contraseña al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que maneja la lógica de cambio de contraseña.
 * @property resourceProvider Proveedor de recursos para obtener cadenas localizadas (mensajes de error).
 */
class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) {
    /**
     * Ejecuta la operación de cambio de contraseña.
     *
     * Realiza validaciones básicas sobre las contraseñas (no vacías, longitud mínima)
     * antes de intentar cambiar la contraseña a través del repositorio.
     *
     * @param currentPassword La contraseña actual del usuario.
     * @param newPassword La nueva contraseña que se desea establecer.
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     * Si es exitoso, contendrá un mensaje de confirmación.
     */
    suspend operator fun invoke(currentPassword: String, newPassword: String): NetworkResponse<String> {
        if (currentPassword.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_password_empty)))
        }
        if (newPassword.isBlank()) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_confirm_password_empty)))
        }
        if (newPassword.length < 6) {
            return NetworkResponse.Error(Exception(resourceProvider.getString(R.string.error_password_min_length)))
        }
        return userRepository.changePassword(currentPassword, newPassword)
    }
}