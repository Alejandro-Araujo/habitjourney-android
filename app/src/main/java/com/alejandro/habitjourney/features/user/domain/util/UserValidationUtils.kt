package com.alejandro.habitjourney.features.user.domain.util

import android.content.Context
import android.util.Patterns
import com.alejandro.habitjourney.R

object UserValidationUtils {

    fun validateEmail(email: String, context: Context): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error(context.getString(R.string.error_email_empty))
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult.Error(context.getString(R.string.error_email_invalid_format))
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String, context: Context): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error(context.getString(R.string.error_password_empty))
            password.length < 6 -> ValidationResult.Error(context.getString(R.string.error_password_min_length))
            !password.matches(Regex(".*[a-zA-Z].*")) ->ValidationResult.Error(context.getString(R.string.error_password_no_letter))
            password.length > 128 -> ValidationResult.Error(context.getString(R.string.error_password_max_length))
            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String, context: Context): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(context.getString(R.string.error_name_empty))
            name.length < 2 -> ValidationResult.Error(context.getString(R.string.error_name_min_length))
            name.trim().length > 50 -> ValidationResult.Error(context.getString(R.string.error_name_max_length))
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> ValidationResult.Error(context.getString(R.string.error_name_invalid_format))
            else -> ValidationResult.Success
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String, context: Context): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error(context.getString(R.string.error_confirm_password_empty))
            password != confirmPassword -> ValidationResult.Error(context.getString(R.string.error_password_mismatch))
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}