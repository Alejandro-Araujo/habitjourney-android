package com.alejandro.habitjourney.features.note.domain.model

import com.alejandro.habitjourney.core.data.local.enums.NoteType

data class Note(
    val id: Long = 0L,
    val userId: Long,
    val title: String,
    val content: String,
    val noteType: NoteType = NoteType.TEXT,
    val listItems: List<NoteListItem> = emptyList(),
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val wordCount: Int = 0,
    val isFavorite: Boolean = false
) {
    // Computed properties
    val isEmpty: Boolean get() = when (noteType) {
        NoteType.TEXT -> title.isBlank() && content.isBlank()
        NoteType.LIST -> title.isBlank() && listItems.isEmpty()
    }

    val preview: String get() = when (noteType) {
        NoteType.TEXT -> content.take(100).replace("\n", " ")
        NoteType.LIST -> {
            val completedCount = listItems.count { it.isCompleted }
            val totalCount = listItems.size
            "$completedCount/$totalCount elementos completados"
        }
    }

    val completionPercentage: Float get() = when (noteType) {
        NoteType.TEXT -> 0f
        NoteType.LIST -> {
            if (listItems.isEmpty()) 0f
            else listItems.count { it.isCompleted }.toFloat() / listItems.size
        }
    }
}