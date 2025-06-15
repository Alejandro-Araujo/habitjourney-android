package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para buscar en las notas activas de un usuario.
 *
 * Esta clase encapsula la lógica para buscar un término específico
 * en el título y contenido de las notas que no están archivadas,
 * proporcionando un flujo (`Flow`) con los resultados.
 *
 * @property noteRepository El repositorio que se utilizará para realizar la búsqueda.
 */
class SearchNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso de búsqueda.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario en cuyas notas se va a buscar.
     * @param searchQuery El texto a buscar.
     * @return Un [Flow] que emite la lista de [Note] que coinciden con la búsqueda.
     */
    operator fun invoke(userId: Long, searchQuery: String): Flow<List<Note>> =
        noteRepository.searchNotes(userId, searchQuery)
}
