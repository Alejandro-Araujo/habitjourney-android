package com.alejandro.habitjourney.features.note.domain.usecase


import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

class GetFavoriteNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: Long) = noteRepository.getFavoriteNotes(userId)
}