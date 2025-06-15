package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para cambiar el estado de "favorito" de una nota.
 *
 * Esta clase encapsula la lógica para marcar o desmarcar una nota como favorita,
 * delegando la operación de persistencia al repositorio.
 *
 * @property noteRepository El repositorio que se utilizará para actualizar el estado de la nota.
 */
class ToggleFavoriteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para cambiar el estado de favorito de una nota.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param noteId El ID de la nota a modificar.
     * @param isFavorite `true` para marcar como favorita, `false` para desmarcar.
     */
    suspend operator fun invoke(noteId: Long, isFavorite: Boolean) {
        noteRepository.setFavorite(noteId, isFavorite)
    }
}
