package com.alejandro.habitjourney.features.user.data.remote.dto.request


import com.google.gson.annotations.SerializedName

/**
 * DTO para la petición de cambio de contraseña de usuario.
 * Corresponde al PasswordChangeDTO del backend.
 */
data class PasswordChangeDTO(
    @SerializedName("currentPassword")
    val currentPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)
