package com.alejandro.habitjourney.features.user.data.mapper


import com.alejandro.habitjourney.features.user.data.entity.UserEntity
import com.alejandro.habitjourney.features.user.domain.model.User
import com.google.firebase.auth.FirebaseUser

// Para mapear de FirebaseUser a tu modelo de dominio User
fun FirebaseUser.toDomainUser(): User {
    return User(
        id = uid,
        name = displayName ?: "", // FirebaseUser tiene displayName
        email = email ?: ""
    )
}

// Para mapear de tu modelo de dominio User a Room Entity
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email
    )
}

/**
 * Mapea una [UserEntity] de Room a un objeto [User] de dominio.
 *
 * @param entity La [UserEntity] a mapear.
 * @return El objeto [User] de dominio correspondiente.
 */
fun UserEntity.toDomainUser(): User {
    return User(
        id = id,
        name = name,
        email = email
    )
}