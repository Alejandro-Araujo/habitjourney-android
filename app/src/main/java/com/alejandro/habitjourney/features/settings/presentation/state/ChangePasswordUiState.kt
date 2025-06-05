package com.alejandro.habitjourney.features.settings.presentation.state


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
    val isValid: Boolean
        get() = currentPasswordError == null &&
                newPasswordError == null &&
                confirmPasswordError == null &&
                currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank()
}