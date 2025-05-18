package com.alejandro.habitjourney.core.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            val errorBody = response.peekBody(Long.MAX_VALUE).string()
            throw ApiException(response.code, response.message, errorBody)
        }

        return response
    }
}

data class ApiException(
    val code: Int,
    override val message: String,
    val errorBody: String?
) : IOException("API error with code: $code, message: $message")