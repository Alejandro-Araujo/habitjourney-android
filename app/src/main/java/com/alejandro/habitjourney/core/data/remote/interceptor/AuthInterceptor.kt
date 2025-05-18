package com.alejandro.habitjourney.core.data.remote.interceptor


import com.alejandro.habitjourney.features.user.data.local.preferences.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si no hay token, procede con la petición original
        val token = userPreferences.getAuthToken()
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