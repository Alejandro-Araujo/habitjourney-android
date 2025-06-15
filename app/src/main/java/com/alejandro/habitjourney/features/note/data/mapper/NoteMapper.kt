package com.alejandro.habitjourney.features.note.data.mapper

import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem
import kotlinx.serialization.json.Json

/**
 * Objeto que proporciona funciones de mapeo entre los modelos de dominio [Note]
 * y las entidades de base de datos [NoteEntity].
 *
 * Centraliza la lógica de conversión para asegurar la coherencia y separar
 * las preocupaciones entre la capa de datos y la capa de dominio.
 */
object NoteMapper {
    /**
     * Convierte un objeto de dominio [Note] a una entidad de base de datos [NoteEntity].
     *
     * Realiza las siguientes transformaciones:
     * - Calcula el [NoteEntity.wordCount] basándose en el tipo de nota.
     * - Serializa la lista de [Note.listItems] a un String JSON si la nota es de tipo [NoteType.LIST].
     *
     * @receiver El objeto [Note] de dominio a convertir.
     * @return Una instancia de [NoteEntity] lista para ser almacenada en la base de datos.
     */
    fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            userId = userId,
            title = title,
            content = content,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            wordCount = when (noteType) {
                NoteType.TEXT -> content.split("\\s+".toRegex()).size
                NoteType.LIST -> listItems.sumOf { it.text.split("\\s+".toRegex()).size }
            },
            isFavorite = isFavorite,
            noteType = noteType.name,
            listItems = if (noteType == NoteType.LIST) {
                Json.encodeToString(listItems)
            } else null
        )
    }

    /**
     * Convierte una entidad de base de datos [NoteEntity] a un objeto de dominio [Note].
     *
     * Realiza las siguientes transformaciones:
     * - Deserializa el String JSON de [NoteEntity.listItems] a una lista de [NoteListItem] si la nota es de tipo lista.
     * - Convierte el nombre del tipo de nota a su correspondiente enum [NoteType].
     *
     * @receiver La entidad [NoteEntity] de la base de datos a convertir.
     * @return Una instancia de [Note] para ser utilizada en la lógica de negocio.
     */
    fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            userId = userId,
            title = title,
            content = content,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt,
            wordCount = wordCount,
            isFavorite = isFavorite,
            noteType = NoteType.valueOf(noteType),
            listItems = listItems?.let {
                Json.decodeFromString<List<NoteListItem>>(it)
                // Si falla la deserialización, devuelve una lista vacía para evitar crashes.
            } ?: emptyList()
        )
    }
}