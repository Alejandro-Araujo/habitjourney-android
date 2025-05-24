package com.alejandro.habitjourney.features.user.data.remote.dto.response

import com.alejandro.habitjourney.features.user.data.remote.dto.UserDTO
import com.google.gson.annotations.SerializedName

/**
 * DTO para respuestas de registro exitoso en Android.
 * Contiene los datos básicos del usuario creado.
 * Corresponde al RegisterResponseDTO del backend.
 */
data class RegisterResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserDTO
)
