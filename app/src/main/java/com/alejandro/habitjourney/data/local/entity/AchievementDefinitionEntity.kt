package com.alejandro.habitjourney.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_definitions",
    primaryKeys = ["id"],
    indices = [Index(value = ["code"], unique = true)])
data class AchievementDefinitionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String
)