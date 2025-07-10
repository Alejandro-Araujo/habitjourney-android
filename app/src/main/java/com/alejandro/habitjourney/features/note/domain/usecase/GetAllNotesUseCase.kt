package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener todos los hábitos de un usuario, incluyendo los activos y archivados.
 *
 * Esta clase encapsula la lógica para solicitar la lista completa de notas de un usuario,
 * proporcionando un flujo (`Flow`) que se actualiza con cualquier cambio en los datos.
 *
 * @property noteRepository El repositorio desde donde se obtendrán los datos de las notas.
 */
class GetAllNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener todas las notas del usuario.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyas notas se van a recuperar.
     * @return Un [Flow] que emite la lista completa de [Note] del usuario.
     */
    operator fun invoke(userId: String): Flow<List<Note>> = noteRepository.getAllNotes(userId)
}
