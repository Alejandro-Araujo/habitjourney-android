package com.alejandro.habitjourney.features.user.presentation.state

/**
 * Clase sellada que representa los diferentes estados de la interfaz de usuario para el proceso de registro.
 *
 * Esto permite modelar el flujo de la UI de registro de manera clara y segura,
 * controlando lo que se muestra al usuario en cada momento (formulario inicial, carga, éxito, error).
 */
sealed class RegisterState {
    /**
     * Estado inicial o por defecto del formulario de registro.
     * El usuario puede introducir sus datos para registrarse.
     */
    data object Initial : RegisterState()
    /**
     * Estado que indica que el proceso de registro está en curso.
     * Generalmente se muestra un indicador de carga.
     */
    data object Loading : RegisterState()
    /**
     * Estado que indica que el registro se ha completado exitosamente.
     * Típicamente, esto precede una navegación a la pantalla principal o de inicio de sesión.
     */
    data object Success : RegisterState()
    /**
     * Estado que indica que ha ocurrido un error durante el proceso de registro.
     *
     * @property message El mensaje descriptivo del error a mostrar al usuario.
     */
    data class Error(val message: String) : RegisterState()
}