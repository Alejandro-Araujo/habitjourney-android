package com.alejandro.habitjourney.features.user.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa la información básica de un usuario.
 * Se utiliza en las respuestas de login y registro.
 * Corresponde al UserDTO del backend.
 */
data class UserDTO(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String
)
