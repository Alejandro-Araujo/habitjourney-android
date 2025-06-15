package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para actualizar los detalles de una nota existente.
 *
 * Esta clase encapsula la lógica para modificar una nota, como su título o contenido,
 * delegando la operación de persistencia al repositorio.
 *
 * @property noteRepository El repositorio que se utilizará para guardar los cambios.
 */
class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para actualizar una nota.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param note El objeto [Note] que contiene los datos actualizados.
     */
    suspend operator fun invoke(note: Note) {
        noteRepository.updateNote(note)
    }
}
