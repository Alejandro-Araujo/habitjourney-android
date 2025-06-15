package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener una lista de todas las notas activas de un usuario.
 *
 * Esta clase encapsula la lógica para recuperar solo las notas que no están archivadas,
 * proporcionando un flujo de datos (`Flow`) que se actualiza automáticamente
 * cuando los datos subyacentes cambian.
 *
 * @property noteRepository El repositorio desde donde se obtendrán los datos.
 */
class GetActiveNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener las notas activas.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyas notas activas se quieren obtener.
     * @return Un [Flow] que emite la lista de [Note] activas.
     */
    operator fun invoke(userId: Long): Flow<List<Note>> = noteRepository.getActiveNotes(userId)
}
