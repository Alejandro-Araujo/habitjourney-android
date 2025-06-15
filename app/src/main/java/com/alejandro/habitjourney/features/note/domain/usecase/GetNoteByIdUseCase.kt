package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener una única nota por su identificador.
 *
 * Esta clase encapsula la lógica para recuperar una nota específica,
 * proporcionando un flujo (`Flow`) para que la UI pueda observar los
 * datos de la nota de forma reactiva.
 *
 * @property noteRepository El repositorio desde donde se obtendrá el dato.
 */
class GetNoteByIdUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener una nota por su ID.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param noteId El ID único de la nota que se desea obtener.
     * @return Un [Flow] que emite la [Note] correspondiente, o `null` si no se encuentra.
     */
    operator fun invoke(noteId: Long): Flow<Note?> = noteRepository.getNoteById(noteId)
}
