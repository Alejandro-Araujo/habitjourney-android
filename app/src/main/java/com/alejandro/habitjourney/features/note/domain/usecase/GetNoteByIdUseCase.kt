package com.alejandro.habitjourney.features.note.domain.usecase


import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteId: Long) = noteRepository.getNoteById(noteId)
}