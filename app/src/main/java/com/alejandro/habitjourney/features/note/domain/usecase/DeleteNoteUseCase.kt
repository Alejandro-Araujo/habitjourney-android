package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar una nota permanentemente.
 *
 * Esta clase encapsula la lógica para borrar una nota de la base de datos
 * a través del repositorio, asegurando una separación clara de responsabilidades.
 *
 * @property noteRepository El repositorio que se utilizará para eliminar la nota.
 */
class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para eliminar la nota.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param noteId El ID de la nota a eliminar permanentemente.
     */
    suspend operator fun invoke(noteId: Long) {
        noteRepository.deleteNote(noteId)
    }
}
