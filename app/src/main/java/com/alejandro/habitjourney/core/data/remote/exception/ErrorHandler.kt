package com.alejandro.habitjourney.core.data.remote.exception

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.interceptor.ApiException
import com.alejandro.habitjourney.core.data.remote.interceptor.NoConnectivityException
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de errores centralizado para toda la aplicación.
 *
 * Esta clase se encarga de traducir las excepciones y errores de bajo nivel
 * (problemas de red, errores de servidor, etc.) en mensajes de texto localizados
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
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoConnectivityException -> applicationContext.getString(R.string.no_internet_connection)
            is SocketTimeoutException -> applicationContext.getString(R.string.connection_timeout)
            is UnknownHostException -> applicationContext.getString(R.string.server_not_found)
            is HttpException -> {
                when (throwable.code()) {
                    401 -> applicationContext.getString(R.string.session_expired)
                    403 -> applicationContext.getString(R.string.permission_denied)
                    404 -> applicationContext.getString(R.string.resource_not_found)
                    500, 501, 502, 503 -> applicationContext.getString(R.string.server_error)
                    else -> applicationContext.getString(R.string.network_error, throwable.code())
                }
            }
            is ApiException -> {
                when (throwable.code) {
                    401 -> applicationContext.getString(R.string.session_expired)
                    403 -> applicationContext.getString(R.string.permission_denied)
                    404 -> applicationContext.getString(R.string.resource_not_found)
                    500, 501, 502, 503 -> applicationContext.getString(R.string.server_error)
                    else -> applicationContext.getString(R.string.api_error, throwable.code, throwable.message)
                }
            }
            is IOException -> applicationContext.getString(R.string.unexpected_network_error)
            else -> applicationContext.getString(R.string.unexpected_error)
        }
    }
}
