package com.alejandro.habitjourney.features.settings.presentation.state

/**
 * Representa el estado de la UI para la pantalla de cambio de contraseña.
 *
 * Contiene los datos del formulario, los posibles errores de validación de cada campo,
 * y las banderas de estado para la operación de cambio de contraseña.
 *
 * @property currentPassword La contraseña actual introducida por el usuario.
 * @property newPassword La nueva contraseña introducida.
 * @property confirmPassword La confirmación de la nueva contraseña.
 * @property currentPasswordError Mensaje de error para el campo de contraseña actual, o null.
 * @property newPasswordError Mensaje de error para el campo de nueva contraseña, o null.
 * @property confirmPasswordError Mensaje de error para el campo de confirmación, o null.
 * @property isLoading `true` si la operación de cambio de contraseña está en curso.
 * @property isSuccess `true` si la contraseña se ha cambiado con éxito.
 * @property errorMessage Un mensaje de error general para la operación (ej: error de red).
 */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Propiedad computada que determina si el formulario es válido para enviar.
     *
     * Requiere que no haya errores de validación y que todos los campos estén rellenos.
     */
    val isValid: Boolean
        get() = currentPasswordError == null &&
                newPasswordError == null &&
                confirmPasswordError == null &&
                currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank()
}