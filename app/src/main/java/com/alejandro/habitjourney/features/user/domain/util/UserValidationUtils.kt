package com.alejandro.habitjourney.features.user.domain.util

import android.content.Context
import android.util.Patterns
import com.alejandro.habitjourney.R

/**
 * Objeto de utilidad que proporciona funciones para validar datos relacionados con el usuario.
 *
 * Ofrece métodos para validar el formato de correo electrónico, la fortaleza de la contraseña,
 * el formato del nombre de usuario y la coincidencia de contraseñas.
 */
object UserValidationUtils {

    /**
     * Valida el formato de una dirección de correo electrónico.
     *
     * @param email La cadena de correo electrónico a validar.
     * @param context El contexto de la aplicación, necesario para acceder a los recursos de strings.
     * @return Un [ValidationResult.Success] si el correo es válido, o un [ValidationResult.Error] con un mensaje si no lo es.
     */
    fun validateEmail(email: String, context: Context): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error(context.getString(R.string.error_email_empty))
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error(context.getString(R.string.error_email_invalid_format))
            else -> ValidationResult.Success
        }
    }

    /**
     * Valida la fortaleza y el formato de una contraseña.
     *
     * Comprueba si la contraseña está en blanco, su longitud mínima y máxima,
     * y si contiene al menos una letra.
     *
     * @param password La cadena de contraseña a validar.
     * @param context El contexto de la aplicación, necesario para acceder a los recursos de strings.
     * @return Un [ValidationResult.Success] si la contraseña es válida, o un [ValidationResult.Error] con un mensaje si no lo es.
     */
    fun validatePassword(password: String, context: Context): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(context.getString(R.string.error_password_empty))
            password.length < 6 -> ValidationResult.Error(context.getString(R.string.error_password_min_length))
            !password.matches(Regex(".*[a-zA-Z].*")) ->ValidationResult.Error(context.getString(R.string.error_password_no_letter))
            password.length > 128 -> ValidationResult.Error(context.getString(R.string.error_password_max_length))
            else -> ValidationResult.Success
        }
    }

    /**
     * Valida el formato y la longitud de un nombre de usuario.
     *
     * Comprueba si el nombre está en blanco, su longitud mínima y máxima,
     * y si contiene solo caracteres alfabéticos, espacios y caracteres acentuados.
     *
     * @param name La cadena del nombre de usuario a validar.
     * @param context El contexto de la aplicación, necesario para acceder a los recursos de strings.
     * @return Un [ValidationResult.Success] si el nombre es válido, o un [ValidationResult.Error] con un mensaje si no lo es.
     */
    fun validateName(name: String, context: Context): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(context.getString(R.string.error_name_empty))
            name.length < 2 -> ValidationResult.Error(context.getString(R.string.error_name_min_length))
            name.trim().length > 50 -> ValidationResult.Error(context.getString(R.string.error_name_max_length))
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> ValidationResult.Error(context.getString(R.string.error_name_invalid_format))
            else -> ValidationResult.Success
        }
    }

    /**
     * Valida que una contraseña de confirmación coincida con la contraseña original.
     *
     * @param password La contraseña original.
     * @param confirmPassword La contraseña de confirmación a comparar.
     * @param context El contexto de la aplicación, necesario para acceder a los recursos de strings.
     * @return Un [ValidationResult.Success] si las contraseñas coinciden y la de confirmación no está en blanco, o un [ValidationResult.Error] con un mensaje si no lo es.
     */
    fun validateConfirmPassword(password: String, confirmPassword: String, context: Context): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error(context.getString(R.string.error_confirm_password_empty))
            password != confirmPassword -> ValidationResult.Error(context.getString(R.string.error_password_mismatch))
            else -> ValidationResult.Success
        }
    }
}

/**
 * Clase sellada que representa el resultado de una operación de validación.
 *
 * Puede ser [ValidationResult.Success] si la validación es exitosa,
 * o [ValidationResult.Error] si la validación falla, conteniendo un mensaje descriptivo del error.
 */
sealed class ValidationResult {
    /**
     * Objeto que representa un resultado de validación exitoso.
     */
    object Success : ValidationResult()
    /**
     * Clase de datos que representa un resultado de validación con error.
     *
     * @property message El mensaje de error que describe la razón de la falla.
     */
    data class Error(val message: String) : ValidationResult()
}