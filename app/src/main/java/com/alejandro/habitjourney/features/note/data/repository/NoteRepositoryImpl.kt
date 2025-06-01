package com.alejandro.habitjourney.features.note.data.repository

import com.alejandro.habitjourney.features.note.data.dao.NoteDao
import com.alejandro.habitjourney.features.note.data.mapper.NoteMapper.toDomain
import com.alejandro.habitjourney.features.note.data.mapper.NoteMapper.toEntity
import com.alejandro.habitjourney.features.note.domain.model.Note
import com.alejandro.habitjourney.features.note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insert(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.update(note.toEntity())
    }

    override suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }

    override fun getNoteById(noteId: Long): Flow<Note?> {
        return noteDao.getNoteById(noteId).map { it?.toDomain() }
    }

    override fun getActiveNotes(userId: Long): Flow<List<Note>> {
        return noteDao.getActiveNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllNotes(userId: Long): Flow<List<Note>> {
        return noteDao.getAllNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedNotes(userId: Long): Flow<List<Note>> {
        return noteDao.getArchivedNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteNotes(userId: Long): Flow<List<Note>> {
        return noteDao.getFavoriteNotes(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchNotes(userId: Long, searchQuery: String): Flow<List<Note>> {
        return noteDao.searchNotes(userId, searchQuery).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun archiveNote(noteId: Long, isArchived: Boolean) {
        noteDao.archiveNote(noteId, isArchived)
    }

    override suspend fun setFavorite(noteId: Long, isFavorite: Boolean) {
        noteDao.setFavorite(noteId, isFavorite)
    }

    override suspend fun getActiveNotesCount(userId: Long): Int {
        return noteDao.getActiveNotesCount(userId)
    }

    override suspend fun getTotalWordCount(userId: Long): Int {
        return noteDao.getTotalWordCount(userId) ?: 0
    }
}