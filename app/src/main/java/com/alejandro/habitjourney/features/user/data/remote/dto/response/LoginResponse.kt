package com.alejandro.habitjourney.features.user.data.remote.dto.response

import com.alejandro.habitjourney.features.user.data.remote.dto.UserDTO
import com.google.gson.annotations.SerializedName

/**
 * DTO para respuestas de inicio de sesión exitoso en Android.
 * Contiene el token JWT generado y los datos básicos del usuario.
 * Corresponde al LoginResponseDTO del backend.
 */
data class LoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: UserDTO
)
