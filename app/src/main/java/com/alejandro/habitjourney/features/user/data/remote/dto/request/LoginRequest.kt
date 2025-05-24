package com.alejandro.habitjourney.features.user.data.remote.dto.request


import com.google.gson.annotations.SerializedName

/**
 * DTO para solicitudes de inicio de sesión en Android.
 * Corresponde al LoginRequestDTO del backend.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
