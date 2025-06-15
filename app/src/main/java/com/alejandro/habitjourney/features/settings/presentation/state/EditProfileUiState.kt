package com.alejandro.habitjourney.features.settings.presentation.state


/**
 * Representa el estado de la UI para la pantalla de edición de perfil.
 *
 * Contiene los datos del formulario del perfil, los posibles errores de validación,
 * y las banderas de estado para la operación de guardado.
 *
 * @property name El nombre actual del usuario en el campo de texto.
 * @property email El email actual del usuario en el campo de texto.
 * @property originalName El nombre original del usuario, para detectar cambios.
 * @property originalEmail El email original del usuario, para detectar cambios.
 * @property nameError Mensaje de error para el campo de nombre, o null.
 * @property emailError Mensaje de error para el campo de email, o null.
 * @property isLoading `true` si la operación de guardado está en curso.
 * @property isSuccess `true` si el perfil se ha guardado con éxito.
 * @property errorMessage Un mensaje de error general para la operación (ej: error de red).
 */
data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val originalName: String = "",
    val originalEmail: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Propiedad computada que determina si ha habido cambios en el formulario.
     */
    val hasChanges: Boolean
        get() = name != originalName || email != originalEmail

    /**
     * Propiedad computada que determina si el formulario es válido para enviar.
     *
     * Requiere que no haya errores de validación y que los campos no estén en blanco.
     */
    val isValid: Boolean
        get() = nameError == null && emailError == null && name.isNotBlank() && email.isNotBlank()
}