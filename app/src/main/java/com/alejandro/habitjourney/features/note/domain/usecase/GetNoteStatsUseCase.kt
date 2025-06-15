package com.alejandro.habitjourney.features.note.domain.usecase

import com.alejandro.habitjourney.features.note.domain.model.NoteStats
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener estadísticas agregadas sobre las notas de un usuario.
 *
 * Esta clase se encarga de la lógica para solicitar datos consolidados,
 * como el número total de notas activas y el conteo total de palabras,
 * y los empaqueta en un objeto [NoteStats].
 *
 * @property noteRepository El repositorio desde donde se obtendrán los datos.
 */
class GetNoteStatsUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Ejecuta el caso de uso para obtener las estadísticas de las notas.
     *
     * La sobrecarga del operador `invoke` permite que la clase sea llamada como si fuera una función.
     *
     * @param userId El ID del usuario cuyas estadísticas se van a calcular.
     * @return Un objeto [NoteStats] que contiene el conteo de notas activas y el conteo total de palabras.
     */
    suspend operator fun invoke(userId: Long): NoteStats {
        val activeCount = noteRepository.getActiveNotesCount(userId)
        val totalWordCount = noteRepository.getTotalWordCount(userId)
        return NoteStats(
            activeNotesCount = activeCount,
            totalWordCount = totalWordCount
        )
    }
}
