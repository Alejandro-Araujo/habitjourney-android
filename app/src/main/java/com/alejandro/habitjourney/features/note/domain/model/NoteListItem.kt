package com.alejandro.habitjourney.features.note.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class NoteListItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val indentLevel: Int = 0,
    val order: Int
) {
    val isEmpty: Boolean get() = text.isBlank()
}