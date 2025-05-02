package com.alejandro.habitjourney.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.alejandro.habitjourney.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // Insertar nota
    @Insert
    suspend fun insert(note: NoteEntity): Long

    // Actualizar nota
    @Update
    suspend fun update(note: NoteEntity): Long

    // Eliminar nota (soft delete)
    @Query("UPDATE notes SET is_deleted = 1 WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)

    // Obtener notas activas del usuario
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_archived = 0 
        AND is_deleted = 0
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveNotesPaged(userId: Long, limit: Int, offset: Int): List<NoteEntity>


    @Query("UPDATE notes SET is_archived = :archived WHERE id = :noteId")
    suspend fun archiveNote(noteId: Long, archived: Boolean)

    // Buscar nota por título o contenido
    @Query("""
        SELECT * FROM notes 
        WHERE user_id = :userId 
        AND is_deleted = 0 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY created_at DESC
    """)
    fun searchNotes(userId: Long, query: String): Flow<List<NoteEntity>>

    // Contar notas activas
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE user_id = :userId
        AND is_archived = 0
        AND is_deleted = 0
    """)
    suspend fun countActiveNotes(userId: Long): Int
}