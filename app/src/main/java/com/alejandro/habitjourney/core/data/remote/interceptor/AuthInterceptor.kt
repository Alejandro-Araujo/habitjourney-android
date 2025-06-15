package com.alejandro.habitjourney.core.data.remote.interceptor

import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que añade automáticamente el token de autenticación a las peticiones HTTP.
 * Si no hay token, deja pasar la petición sin modificar (para endpoints públicos).
 */
class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = userPreferences.getAuthTokenSync()

        // Si no hay token, procede con la petición original
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Añade el token de autenticación al header
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}