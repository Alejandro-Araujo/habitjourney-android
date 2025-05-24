package com.alejandro.habitjourney.features.user.data.remote.dto.request


import com.google.gson.annotations.SerializedName

/**
 * DTO para solicitudes de registro de nuevo usuario en Android.
 * Corresponde al RegisterRequestDTO del backend.
 */
data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)
