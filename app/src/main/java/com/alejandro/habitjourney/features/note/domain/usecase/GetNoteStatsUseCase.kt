package com.alejandro.habitjourney.features.note.domain.usecase


import com.alejandro.habitjourney.features.note.domain.model.NoteStats
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

class GetNoteStatsUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(userId: Long): NoteStats {
        val activeCount = noteRepository.getActiveNotesCount(userId)
        val totalWordCount = noteRepository.getTotalWordCount(userId)
        return NoteStats(
            activeNotesCount = activeCount,
            totalWordCount = totalWordCount
        )
    }
}