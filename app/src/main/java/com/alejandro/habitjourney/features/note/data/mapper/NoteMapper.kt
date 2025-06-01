package com.alejandro.habitjourney.features.note.data.mapper

import com.alejandro.habitjourney.core.data.local.enums.NoteType
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.model.NoteListItem
import kotlinx.serialization.json.Json


object NoteMapper {
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
            } ?: emptyList()
        )
    }
}