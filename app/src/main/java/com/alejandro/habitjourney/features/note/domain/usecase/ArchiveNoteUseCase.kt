package com.alejandro.habitjourney.features.note.domain.usecase


import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para cambiar el estado de archivado de una nota.
 *
 * Esta clase encapsula la lógica para archivar o desarchivar una nota,
 * permitiendo que la UI ejecute esta acción a través de una única dependencia.
 *
 * @property noteRepository El repositorio que se utilizará para persistir el cambio de estado.
 */
class ArchiveNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para archivar o desarchivar una nota.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param noteId El ID de la nota cuyo estado de archivado se va a cambiar.
     * @param isArchived `true` para archivar la nota, `false` para desarchivarla.
     */
    suspend operator fun invoke(noteId: Long, isArchived: Boolean) {
        noteRepository.archiveNote(noteId, isArchived)
    }
}
