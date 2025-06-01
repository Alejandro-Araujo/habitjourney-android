package com.alejandro.habitjourney.features.note.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: Long): Flow<NoteEntity?>

    // Notas activas (no archivadas)
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 0 
        ORDER BY updated_at DESC
    """)
    fun getActiveNotes(userId: Long): Flow<List<NoteEntity>>

    // Todas las notas (incluye archivadas)
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        ORDER BY updated_at DESC
    """)
    fun getAllNotes(userId: Long): Flow<List<NoteEntity>>

    // Notas archivadas
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 1 
        ORDER BY updated_at DESC
    """)
    fun getArchivedNotes(userId: Long): Flow<List<NoteEntity>>

    // Notas favoritas
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_favorite = 1 
        AND is_archived = 0 
        ORDER BY updated_at DESC
    """)
    fun getFavoriteNotes(userId: Long): Flow<List<NoteEntity>>

    // Búsqueda
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 0 
        AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%')
        ORDER BY updated_at DESC
    """)
    fun searchNotes(userId: Long, searchQuery: String): Flow<List<NoteEntity>>

    // Archivar/desarchivar
    @Query("UPDATE notes SET is_archived = :isArchived WHERE id = :noteId")
    suspend fun archiveNote(noteId: Long, isArchived: Boolean)

    // Marcar/desmarcar favorito
    @Query("UPDATE notes SET is_favorite = :isFavorite WHERE id = :noteId")
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)

    // Eliminar permanentemente
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    // Estadísticas
    @Query("SELECT COUNT(*) FROM notes WHERE user_id = :userId AND is_archived = 0")
    suspend fun getActiveNotesCount(userId: Long): Int

    @Query("SELECT SUM(word_count) FROM notes WHERE user_id = :userId AND is_archived = 0")
    suspend fun getTotalWordCount(userId: Long): Int?
}