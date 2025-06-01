package com.alejandro.habitjourney.features.note.domain.repository

import com.alejandro.habitjourney.features.note.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(noteId: Long)
    fun getNoteById(noteId: Long): Flow<Note?>
    fun getActiveNotes(userId: Long): Flow<List<Note>>
    fun getAllNotes(userId: Long): Flow<List<Note>>
    fun getArchivedNotes(userId: Long): Flow<List<Note>>
    fun getFavoriteNotes(userId: Long): Flow<List<Note>>
    fun searchNotes(userId: Long, searchQuery: String): Flow<List<Note>>
    suspend fun archiveNote(noteId: Long, isArchived: Boolean)
    suspend fun setFavorite(noteId: Long, isFavorite: Boolean)
    suspend fun getActiveNotesCount(userId: Long): Int
    suspend fun getTotalWordCount(userId: Long): Int
}