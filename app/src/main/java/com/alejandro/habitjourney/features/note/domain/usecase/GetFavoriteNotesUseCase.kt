package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todas las notas favoritas y activas de un usuario.
 *
 * Esta clase encapsula la lógica para solicitar una lista de notas que
 * han sido marcadas como favoritas y que no están archivadas, proporcionando
 * un flujo (`Flow`) que se actualiza con cualquier cambio en los datos.
 *
 * @property noteRepository El repositorio desde donde se obtendrán los datos de las notas.
 */
class GetFavoriteNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener las notas favoritas del usuario.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyas notas favoritas se van a recuperar.
     * @return Un [Flow] que emite la lista de [Note] favoritas y activas del usuario.
     */
    operator fun invoke(userId: Long): Flow<List<Note>> = noteRepository.getFavoriteNotes(userId)
}
