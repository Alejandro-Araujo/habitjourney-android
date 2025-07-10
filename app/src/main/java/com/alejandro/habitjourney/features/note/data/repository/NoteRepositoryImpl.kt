package com.alejandro.habitjourney.features.note.data.repository

import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.note.data.mapper.NoteMapper.toDomain
import com.alejandro.habitjourney.features.note.data.mapper.NoteMapper.toEntity
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del [NoteRepository] que gestiona los datos de las notas.
 *
 * Esta clase actúa como la única fuente de verdad para los datos de las notas,
 * coordinando las operaciones con el [NoteDao] y utilizando [NoteMapper] para
 * convertir entre las entidades de la base de datos y los modelos de dominio.
 *
 * @property noteDao El Data Access Object para interactuar con la tabla de notas.
 */
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    /**
     * Inserta una nueva nota en la base de datos.
     * @param note El modelo de dominio [Note] a insertar.
     * @return El ID de la nota recién creada.
     */
    override suspend fun insertNote(note: Note): Long {
        return noteDao.insert(note.toEntity())
    }

    /**
     * Actualiza una nota existente en la base de datos.
     * @param note El modelo de dominio [Note] con los datos actualizados.
     */
    override suspend fun updateNote(note: Note) {
        noteDao.update(note.toEntity())
    }

    /**
     * Elimina una nota permanentemente por su ID.
     * @param noteId El ID de la nota a eliminar.
     */
    override suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }

    /**
     * Obtiene una nota específica por su ID.
     * @param noteId El ID de la nota.
     * @return Un [Flow] que emite la [Note] correspondiente o null si no se encuentra.
     */
    override fun getNoteById(noteId: Long): Flow<Note?> {
        return noteDao.getNoteById(noteId).map { it?.toDomain() }
    }

    /**
     * Obtiene una lista de todas las notas activas (no archivadas) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas activas.
     */
    override fun getActiveNotes(userId: String): Flow<List<Note>> {
        return noteDao.getActiveNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene una lista de todas las notas (activas y archivadas) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista completa de notas.
     */
    override fun getAllNotes(userId: String): Flow<List<Note>> {
        return noteDao.getAllNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene una lista de todas las notas archivadas de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas archivadas.
     */
    override fun getArchivedNotes(userId: String): Flow<List<Note>> {
        return noteDao.getArchivedNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene una lista de todas las notas favoritas (y no archivadas) de un usuario.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas favoritas.
     */
    override fun getFavoriteNotes(userId: String): Flow<List<Note>> {
        return noteDao.getFavoriteNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Busca en las notas activas de un usuario por título o contenido.
     * @param userId El ID del usuario.
     * @param searchQuery El texto a buscar.
     * @return Un [Flow] que emite la lista de notas que coinciden con la búsqueda.
     */
    override fun searchNotes(userId: String, searchQuery: String): Flow<List<Note>> {
        return noteDao.searchNotes(userId, searchQuery).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Cambia el estado de archivado de una nota.
     * @param noteId El ID de la nota.
     * @param isArchived `true` para archivar, `false` para desarchivar.
     */
    override suspend fun archiveNote(noteId: Long, isArchived: Boolean) {
        noteDao.archiveNote(noteId, isArchived)
    }

    /**
     * Marca o desmarca una nota como favorita.
     * @param noteId El ID de la nota.
     * @param isFavorite `true` para marcar como favorita, `false` para desmarcar.
     */
    override suspend fun setFavorite(noteId: Long, isFavorite: Boolean) {
        noteDao.setFavorite(noteId, isFavorite)
    }

    /**
     * Obtiene el número total de notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El número de notas activas.
     */
    override suspend fun getActiveNotesCount(userId: String): Int {
        return noteDao.getActiveNotesCount(userId)
    }

    /**
     * Obtiene la suma total de palabras de todas las notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El conteo total de palabras. Devuelve 0 si no hay notas.
     */
    override suspend fun getTotalWordCount(userId: String): Int {
        return noteDao.getTotalWordCount(userId) ?: 0
    }
}
