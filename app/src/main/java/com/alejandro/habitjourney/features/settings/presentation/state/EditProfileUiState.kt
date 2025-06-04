package com.alejandro.habitjourney.features.settings.presentation.state


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
    val hasChanges: Boolean
        get() = name != originalName || email != originalEmail

    val isValid: Boolean
        get() = nameError == null && emailError == null && name.isNotBlank() && email.isNotBlank()
}