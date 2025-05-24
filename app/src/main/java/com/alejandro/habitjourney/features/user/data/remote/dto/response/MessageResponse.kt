package com.alejandro.habitjourney.features.user.data.remote.dto.response

import com.google.gson.annotations.SerializedName

/**
 * DTO para respuestas simples que solo contienen un mensaje.
 * Corresponde al MessageResponse del backend para endpoints como cambiar contraseña o eliminar usuario.
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String
)