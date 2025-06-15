package com.alejandro.habitjourney.features.note.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de la entidad [NoteEntity].
 *
 * Proporciona todos los métodos necesarios para interactuar con la tabla 'notes'
 * en la base de datos de Room.
 */
@Dao
interface NoteDao {

    /**
     * Inserta una nueva nota. Si ya existe una nota con el mismo ID, la reemplaza.
     * @param note La [NoteEntity] a insertar.
     * @return El ID de la fila de la nota insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    /**
     * Actualiza una nota existente.
     * @param note La [NoteEntity] con los datos actualizados.
     */
    @Update
    suspend fun update(note: NoteEntity)

    /**
     * Obtiene una nota específica por su ID.
     * @param noteId El ID de la nota a recuperar.
     * @return Un [Flow] que emite la [NoteEntity] o null si no se encuentra.
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<NoteEntity?>

    /**
     * Obtiene todas las notas activas (no archivadas) de un usuario, ordenadas por la más reciente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas activas.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 0 
        ORDER BY updated_at DESC
    """)
    fun getActiveNotes(userId: Long): Flow<List<NoteEntity>>

    /**
     * Obtiene todas las notas de un usuario, incluyendo las archivadas, ordenadas por la más reciente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista completa de notas.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        ORDER BY updated_at DESC
    """)
    fun getAllNotes(userId: Long): Flow<List<NoteEntity>>

    /**
     * Obtiene todas las notas archivadas de un usuario, ordenadas por la más reciente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas archivadas.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 1 
        ORDER BY updated_at DESC
    """)
    fun getArchivedNotes(userId: Long): Flow<List<NoteEntity>>

    /**
     * Obtiene todas las notas favoritas y activas de un usuario, ordenadas por la más reciente.
     * @param userId El ID del usuario.
     * @return Un [Flow] que emite la lista de notas favoritas.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_favorite = 1 
        AND is_archived = 0 
        ORDER BY updated_at DESC
    """)
    fun getFavoriteNotes(userId: Long): Flow<List<NoteEntity>>

    /**
     * Busca en las notas activas de un usuario por título o contenido.
     * @param userId El ID del usuario.
     * @param searchQuery El texto a buscar.
     * @return Un [Flow] que emite la lista de notas que coinciden con la búsqueda.
     */
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 0 
        AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%')
        ORDER BY updated_at DESC
    """)
    fun searchNotes(userId: Long, searchQuery: String): Flow<List<NoteEntity>>

    /**
     * Cambia el estado de archivado de una nota.
     * @param noteId El ID de la nota a modificar.
     * @param isArchived `true` para archivar, `false` para desarchivar.
     */
    @Query("UPDATE notes SET is_archived = :isArchived WHERE id = :noteId")
    suspend fun archiveNote(noteId: Long, isArchived: Boolean)

    /**
     * Marca o desmarca una nota como favorita.
     * @param noteId El ID de la nota a modificar.
     * @param isFavorite `true` para marcar como favorita, `false` para desmarcar.
     */
    @Query("UPDATE notes SET is_favorite = :isFavorite WHERE id = :noteId")
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)

    /**
     * Elimina una nota permanentemente de la base de datos.
     * @param noteId El ID de la nota a eliminar.
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    /**
     * Obtiene el número total de notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El número de notas activas.
     */
    @Query("SELECT COUNT(*) FROM notes WHERE user_id = :userId AND is_archived = 0")
    suspend fun getActiveNotesCount(userId: Long): Int

    /**
     * Obtiene la suma total de palabras de todas las notas activas de un usuario.
     * @param userId El ID del usuario.
     * @return El conteo total de palabras, o null si no hay notas.
     */
    @Query("SELECT SUM(word_count) FROM notes WHERE user_id = :userId AND is_archived = 0")
    suspend fun getTotalWordCount(userId: Long): Int?
}
