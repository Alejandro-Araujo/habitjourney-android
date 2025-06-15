package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las notas archivadas de un usuario.
 *
 * Esta clase encapsula la lógica para solicitar una lista de notas que
 * han sido archivadas, proporcionando un flujo (`Flow`) que se actualiza
 * con cualquier cambio en los datos.
 *
 * @property noteRepository El repositorio desde donde se obtendrán los datos de las notas.
 */
class GetArchivedNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener las notas archivadas del usuario.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyas notas archivadas se van a recuperar.
     * @return Un [Flow] que emite la lista de [Note] archivadas del usuario.
     */
    operator fun invoke(userId: Long): Flow<List<Note>> = noteRepository.getArchivedNotes(userId)
}
