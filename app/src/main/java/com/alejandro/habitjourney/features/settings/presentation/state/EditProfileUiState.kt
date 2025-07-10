package com.alejandro.habitjourney.features.settings.presentation.state

import com.alejandro.habitjourney.features.user.presentation.state.ReauthenticationState


/**
 * Representa el estado de la UI para la pantalla de edición de perfil.
 *
 * Contiene los datos del formulario del perfil, los posibles errores de validación,
 * las banderas de estado para la operación de guardado, y el estado de verificación de email.
 *
 * @property name El nombre actual del usuario en el campo de texto.
 * @property email El email actual del usuario en el campo de texto.
 * @property originalName El nombre original del usuario, para detectar cambios.
 * @property originalEmail El email original del usuario, para detectar cambios.
 * @property nameError Mensaje de error para el campo de nombre, o null.
 * @property emailError Mensaje de error para el campo de email, o null.
 * @property isLoading `true` si la operación de guardado está en curso.
 * @property isSuccess `true` si el perfil se ha guardado con éxito.
 * @property emailVerificationSent `true` si se envió un email de verificación tras cambiar el email.
 * @property errorMessage Un mensaje de error general para la operación (ej: error de red).
 * @property forceSignOut `true` si se debe forzar el logout del usuario.
 * @property showForceSignOutDialog `true` si se debe mostrar el diálogo de confirmación.
 * @property logoutCompleted `true` si el logout se completó exitosamente.
 * @property isEmailVerified `true` si el email del usuario está verificado.
 * @property reauthState Estado de la reautenticación.
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
    val emailVerificationSent: Boolean = false,
    val errorMessage: String? = null,
    val forceSignOut: Boolean = false,
    val showForceSignOutDialog: Boolean = false,
    val logoutCompleted: Boolean = false,
    val isEmailVerified: Boolean = false,
    val reauthState: ReauthenticationState = ReauthenticationState()
) {
    /**
     * Propiedad computada que determina si ha habido cambios en el formulario.
     */
    val hasChanges: Boolean
        get() = name != originalName || email != originalEmail


    /**
     * Propiedad computada que determina si el email ha cambiado.
     */
    val emailChanged: Boolean
        get() = email != originalEmail


    /**
     * Propiedad computada que determina si el formulario es válido para enviar.
     *
     * Requiere que no haya errores de validación y que los campos no estén en blanco.
     */
    val isValid: Boolean
        get() = nameError == null && emailError == null && name.isNotBlank() && email.isNotBlank()
}