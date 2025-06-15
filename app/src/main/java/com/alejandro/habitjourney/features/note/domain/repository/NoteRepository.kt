package com.alejandro.habitjourney.features.note.domain.repository

import com.alejandro.habitjourney.features.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define el contrato para la gestión de datos de las notas.
 *
 * Actúa como una abstracción sobre la capa de datos, permitiendo que el dominio
 * interactúe con los datos de las notas sin conocer los detalles de su implementación
 * (ej: base de datos local, API remota).
 */
interface NoteRepository {
    /**
     * Inserta una nueva nota.
     * @param note El modelo [Note] a crear.
     * @return El ID de la nota recién creada.
     */
    suspend fun insertNote(note: Note): Long

    /**
     * Actualiza una nota existente.
     * @param note El modelo [Note] con los datos actualizados.
     */
    suspend fun updateNote(note: Note)

    /**
     * Elimina permanentemente una nota por su ID.
     * @param noteId El ID de la nota a eliminar.
     */
    suspend fun deleteNote(noteId: Long)

    /**
     * Obtiene una nota específica por su ID.
     * @param noteId El ID de la nota a recuperar.
     * @return Un [Flow] que emite la [Note] o null si no se encuentra.
     */
    fun getNoteById(noteId: Long): Flow<Note?>

    /**
     * Obtiene una lista de todas las notas activas (no archivadas) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas activas.
     */
    fun getActiveNotes(userId: Long): Flow<List<Note>>

    /**
     * Obtiene todas las notas de un usuario, incluyendo las archivadas.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista completa de notas del usuario.
     */
    fun getAllNotes(userId: Long): Flow<List<Note>>

    /**
     * Obtiene todas las notas archivadas de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas archivadas.
     */
    fun getArchivedNotes(userId: Long): Flow<List<Note>>

    /**
     * Obtiene todas las notas favoritas (y no archivadas) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas favoritas.
     */
    fun getFavoriteNotes(userId: Long): Flow<List<Note>>

    /**
     * Busca en las notas activas de un usuario por título o contenido.
     * @param userId El ID del usuario.
     * @param searchQuery El texto a buscar.
     * @return Un [Flow] que emite la lista de notas que coinciden con la búsqueda.
     */
    fun searchNotes(userId: Long, searchQuery: String): Flow<List<Note>>

    /**
     * Cambia el estado de archivado de una nota.
     * @param noteId El ID de la nota a modificar.
     * @param isArchived `true` para archivar, `false` para desarchivar.
     */
    suspend fun archiveNote(noteId: Long, isArchived: Boolean)

    /**
     * Marca o desmarca una nota como favorita.
     * @param noteId El ID de la nota a modificar.
     * @param isFavorite `true` para marcar como favorita, `false` para desmarcar.
     */
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)

    /**
     * Obtiene el número total de notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El número de notas activas.
     */
    suspend fun getActiveNotesCount(userId: Long): Int

    /**
     * Obtiene la suma total de palabras de todas las notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El conteo total de palabras.
     */
    suspend fun getTotalWordCount(userId: Long): Int
}
