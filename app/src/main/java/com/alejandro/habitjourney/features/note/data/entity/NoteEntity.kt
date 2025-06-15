package com.alejandro.habitjourney.features.note.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alejandro.habitjourney.features.user.data.local.entity.UserEntity


/**
 * Representa una nota en la base de datos local.
 *
 * Esta entidad está vinculada a un [UserEntity] a través de una clave externa,
 * asegurando que cada nota pertenezca a un usuario. Incluye índices para optimizar
 * las consultas comunes de filtrado y ordenación.
 *
 * @property id El identificador único autogenerado para la nota.
 * @property userId El ID del [UserEntity] al que pertenece esta nota.
 * @property title El título de la nota.
 * @property content El contenido principal de la nota en formato de texto.
 * @property noteType El tipo de nota (ej: "TEXT", "CHECKLIST"). Futura implementación.
 * @property listItems Contenido serializado para notas de tipo lista. Futura implementación.
 * @property isArchived `true` si la nota está archivada y no debe mostrarse en la lista principal.
 * @property createdAt Timestamp de la creación de la nota.
 * @property updatedAt Timestamp de la última modificación de la nota.
 * @property wordCount El número de palabras en el contenido de la nota, para estadísticas.
 * @property isFavorite `true` si la nota está marcada como favorita por el usuario.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borran sus notas.
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["is_archived"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"]),
        Index(value = ["is_favorite"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "note_type")
    val noteType: String = "TEXT",

    @ColumnInfo(name = "list_items")
    val listItems: String? = null,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "word_count")
    val wordCount: Int = 0,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)

