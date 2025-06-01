package com.alejandro.habitjourney.features.note.domain.usecase



import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

class ToggleFavoriteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: Long, isFavorite: Boolean) {
        noteRepository.setFavorite(noteId, isFavorite)
    }
}