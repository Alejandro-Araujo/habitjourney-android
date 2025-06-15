package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para crear una nueva nota.
 *
 * Esta clase encapsula la lógica de negocio para la creación de una nota,
 * delegando la operación de persistencia al repositorio.
 *
 * @property noteRepository El repositorio que se utilizará para insertar la nota.
 */
class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para crear una nota.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param note El objeto [Note] de dominio que se va to crear.
     * @return El ID (`Long`) de la nota recién creada.
     */
    suspend operator fun invoke(note: Note): Long {
        return noteRepository.insertNote(note)
    }
}
