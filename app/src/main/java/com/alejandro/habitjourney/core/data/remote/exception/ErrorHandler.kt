package com.alejandro.habitjourney.core.data.remote.exception

import android.annotation.SuppressLint
import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.utils.logging.AppLogger
import com.alejandro.habitjourney.features.user.domain.exception.ReauthenticationRequiredException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de errores centralizado para toda la aplicación.
 *
 * Esta clase se encarga de traducir las excepciones y errores de bajo nivel
 * (problemas de red, errores de Firebase Auth, etc.) en mensajes de texto localizados
 * y comprensibles para el usuario final.
 *
 * @property applicationContext El contexto de la aplicación, inyectado por Hilt,
 * para acceder a los recursos de strings.
 */
@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    /**
     * Convierte un [Throwable] en un mensaje de error legible para el usuario.
     *
     * Analiza el tipo de excepción y devuelve el string localizado correspondiente
     * desde los recursos de la aplicación.
     *
     * @param throwable La excepción o error que se ha producido.
     * @return Un [String] que representa el mensaje de error para mostrar en la UI.
     */
    @SuppressLint("StringFormatInvalid")
    fun getErrorMessage(throwable: Throwable): String {
        AppLogger.e("ErrorHandler", "Unhandled throwable type: ${throwable::class.java.simpleName}, message: ${throwable.message}", throwable)
        return when (throwable) {
            is SocketTimeoutException -> applicationContext.getString(R.string.connection_timeout)
            is UnknownHostException -> applicationContext.getString(R.string.server_not_found)
            is IOException -> applicationContext.getString(R.string.unexpected_network_error)
            is ReauthenticationRequiredException -> {
                throwable.message ?: applicationContext.getString(R.string.error_recent_login_required)
            }
            is FirebaseAuthUserCollisionException -> {
                throwable.message ?: applicationContext.getString(R.string.error_email_already_exists_try_signin)
            }

            is FirebaseAuthException -> {
                when (throwable.errorCode) {
                    "ERROR_INVALID_CUSTOM_TOKEN" -> applicationContext.getString(R.string.firebase_error_invalid_custom_token)
                    "ERROR_CUSTOM_TOKEN_MISMATCH" -> applicationContext.getString(R.string.firebase_error_custom_token_mismatch)
                    "ERROR_INVALID_CREDENTIAL" -> applicationContext.getString(R.string.firebase_error_invalid_credential)
                    "ERROR_INVALID_EMAIL" -> applicationContext.getString(R.string.firebase_error_invalid_email)
                    "ERROR_WRONG_PASSWORD" -> applicationContext.getString(R.string.firebase_error_wrong_password)
                    "ERROR_USER_NOT_FOUND" -> applicationContext.getString(R.string.firebase_error_user_not_found)
                    "ERROR_USER_DISABLED" -> applicationContext.getString(R.string.firebase_error_user_disabled)
                    "ERROR_TOO_MANY_REQUESTS" -> applicationContext.getString(R.string.firebase_error_too_many_requests)
                    "ERROR_OPERATION_NOT_ALLOWED" -> applicationContext.getString(R.string.firebase_error_operation_not_allowed)
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> applicationContext.getString(R.string.firebase_error_account_exists_with_different_credential)
                    "ERROR_WEAK_PASSWORD" -> applicationContext.getString(R.string.firebase_error_weak_password)
                    "ERROR_EMAIL_ALREADY_IN_USE" -> applicationContext.getString(R.string.firebase_error_email_already_in_use)
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> applicationContext.getString(R.string.firebase_error_credential_already_in_use)
                    "ERROR_REQUIRES_RECENT_LOGIN" -> applicationContext.getString(R.string.firebase_error_requires_recent_login)
                    else -> applicationContext.getString(R.string.firebase_error_generic, throwable.message)
                }
            }

            else -> applicationContext.getString(R.string.unexpected_error)
        }
    }
}