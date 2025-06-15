package com.alejandro.habitjourney.core.data.remote.network

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.exception.ErrorHandler
import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import retrofit2.HttpException
import retrofit2.Response

/**
 * Ejecuta llamadas de API de forma segura, convirtiendo excepciones en NetworkResponse.
 *
 * @param apiCall La función que hace la llamada a Retrofit
 * @return NetworkResponse con el resultado o error
 */
suspend fun <T> safeApiCall(
    context: Context,
    errorHandler: ErrorHandler,
    apiCall: suspend () -> Response<T>
): NetworkResponse<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResponse.Success(body)
            } else {
                NetworkResponse.Error(NullPointerException(context.getString(R.string.response_body_null)))
            }
        } else {
            NetworkResponse.Error(HttpException(response))
        }
    } catch (e: Exception) {
        NetworkResponse.Error(e)
    }
}