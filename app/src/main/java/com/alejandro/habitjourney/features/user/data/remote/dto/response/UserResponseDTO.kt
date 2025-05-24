package com.alejandro.habitjourney.features.user.data.remote.dto.response

import com.alejandro.habitjourney.features.user.data.remote.dto.UserDTO
import com.google.gson.annotations.SerializedName

/**
 * DTO para la estructura de respuesta estándar con datos de usuario.
 * Se utiliza para endpoints como /api/users/me.
 * Corresponde al UserResponseDTO del backend.
 */
data class UserResponseDTO(
    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: UserDTO
)