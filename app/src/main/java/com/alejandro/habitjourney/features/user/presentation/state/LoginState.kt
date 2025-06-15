package com.alejandro.habitjourney.features.user.presentation.state

/**
 * Clase sellada que representa los diferentes estados de la interfaz de usuario para el proceso de inicio de sesión.
 *
 * Esto permite modelar el flujo de la UI de inicio de sesión de manera clara y segura,
 * controlando lo que se muestra al usuario en cada momento (formulario inicial, carga, éxito, error).
 */
sealed class LoginState {
    /**
     * Estado inicial o por defecto del formulario de inicio de sesión.
     * El usuario puede introducir sus credenciales.
     */
    data object Initial : LoginState()
    /**
     * Estado que indica que el proceso de inicio de sesión está en curso.
     * Generalmente se muestra un indicador de carga.
     */
    data object Loading : LoginState()
    /**
     * Estado que indica que el inicio de sesión se ha completado exitosamente.
     * Típicamente, esto precede una navegación a la pantalla principal de la aplicación.
     */
    data object Success : LoginState()
    /**
     * Estado que indica que ha ocurrido un error durante el proceso de inicio de sesión.
     *
     * @property message El mensaje descriptivo del error a mostrar al usuario.
     */
    data class Error(val message: String) : LoginState()
}