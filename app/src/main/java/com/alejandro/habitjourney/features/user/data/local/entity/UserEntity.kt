package com.alejandro.habitjourney.features.user.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar la información básica del usuario autenticado.
 * Esta entidad representa el perfil del usuario en la base de datos local.
 * NO debe contener información sensible como contraseñas.
 */
@Entity(tableName = "users",
    indices = [Index(value = ["email"], unique = true)])

data class UserEntity(
    @PrimaryKey (autoGenerate = false)
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo (name = "email")
    val email: String
)
