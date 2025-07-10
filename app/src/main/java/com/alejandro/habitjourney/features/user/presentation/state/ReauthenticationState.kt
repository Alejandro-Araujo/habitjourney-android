package com.alejandro.habitjourney.features.user.presentation.state


/**
 * Estado común para manejar reautenticación en diferentes pantallas.
 */
data class ReauthenticationState(
    val showDialog: Boolean = false,
    val type: ReauthenticationType? = null,
    val passwordInput: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccessfullReauthAndAction: Boolean = false
)

enum class ReauthenticationType {
    EMAIL_PASSWORD,
    GOOGLE
}