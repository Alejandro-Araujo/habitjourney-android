package com.alejandro.habitjourney.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    indices = [Index(value = ["email"], unique = true)])
data class UserEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo (name = "email", index = true)
    val email: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)