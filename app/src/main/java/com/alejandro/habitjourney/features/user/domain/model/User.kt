package com.alejandro.habitjourney.features.user.domain.model

/**
 * Representa el modelo de dominio de un usuario.
 *
 * Este modelo encapsula la información esencial de un usuario tal como se maneja
 * en la capa de negocio de la aplicación. No incluye detalles de implementación
 * específicos de la base de datos o de la red, asegurando una clara separación de responsabilidades.
 *
 * @property id El identificador único del usuario.
 * @property name El nombre completo o nombre de usuario del usuario.
 * @property email La dirección de correo electrónico del usuario, utilizada a menudo para la autenticación.
 */
data class User(
    val id: Long,
    val name: String,
    val email: String,
)