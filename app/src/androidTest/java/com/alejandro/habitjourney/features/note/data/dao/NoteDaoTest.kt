package com.alejandro.habitjourney.features.note.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.note.data.entity.NoteEntity
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        noteDao = database.noteDao()
        userDao = database.userDao()

        // Crear un usuario para las pruebas
        coroutineRule.runTest {
            val user = TestDataFactory.createUserEntity(
                name = "testuser",
                email = "test@example.com"
            )
            userId = userDao.insertUser(user)
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertNote_returnsId() = coroutineRule.runTest {
        // Given
        val note = createTestNote()

        // When
        val noteId = noteDao.insert(note)

        // Then
        assertTrue(noteId > 0)
    }

    @Test
    fun updateNote_updatesCorrectNote() = coroutineRule.runTest {
        // Given
        val note = createTestNote()
        val noteId = noteDao.insert(note)
        val updatedNote = note.copy(
            id = noteId,
            title = "Updated Note",
            content = "Updated content"
        )

        // When
        noteDao.update(updatedNote)

        // Then
        val result = noteDao.getActiveNotesPaged(userId, 10, 0).first { it.id == noteId }
        assertEquals("Updated Note", result.title)
        assertEquals("Updated content", result.content)
    }

    @Test
    fun getActiveNotesPaged_returnsOnlyActiveNotes() = coroutineRule.runTest {
        // Given
        // Insert active notes
        val activeNotes = listOf(
            createTestNote(title = "Active Note 1"),
            createTestNote(title = "Active Note 2"),
            createTestNote(title = "Active Note 3")
        )
        activeNotes.forEach { noteDao.insert(it) }

        // Insert archived note
        val archivedNote = createTestNote(title = "Archived Note", isArchived = true)
        noteDao.insert(archivedNote)

        // Insert deleted note
        val deletedNote = createTestNote(title = "Deleted Note", isDeleted = true)
        noteDao.insert(deletedNote)

        // When
        val result = noteDao.getActiveNotesPaged(userId, 10, 0)

        // Then
        assertEquals(3, result.size)
        result.forEach { note ->
            assertFalse(note.isArchived)
            assertFalse(note.isDeleted)
        }
    }

    @Test
    fun archiveNote_marksNoteAsArchived() = coroutineRule.runTest {
        // Given
        val note = createTestNote()
        val noteId = noteDao.insert(note)

        // When
        noteDao.archiveNote(noteId, true)

        // Then
        val activeNotes = noteDao.getActiveNotesPaged(userId, 10, 0)
        assertTrue(activeNotes.none { it.id == noteId })
    }

    @Test
    fun deleteNote_marksNoteAsDeleted() = coroutineRule.runTest {
        // Given
        val note = createTestNote()
        val noteId = noteDao.insert(note)

        // When
        noteDao.deleteNote(noteId)

        // Then
        val activeNotes = noteDao.getActiveNotesPaged(userId, 10, 0)
        assertTrue(activeNotes.none { it.id == noteId })
    }

    @Test
    fun searchNotes_returnsMatchingNotes() = coroutineRule.runTest {
        // Given
        // Insert notes with different titles and content
        val notes = listOf(
            createTestNote(title = "Planificar cumpleaños", content = "Planificar fiesta sorpresa de cumpleaños"),
            createTestNote(title = "Lista de la compra", content = "Leche, huevos, pan"),
            createTestNote(title = "Tareas del curro", content = "Plantear nueva propuesta de proyecto"),
            createTestNote(title = "Libros para leer", content = "El señor de las moscas")
        )
        notes.forEach { noteDao.insert(it) }

        // When
        val resultForPlan = noteDao.searchNotes(userId, "plan").first()

        // Then
        assertEquals(2, resultForPlan.size)
        assertTrue(resultForPlan.any { it.title == "Planificar cumpleaños" })
        assertTrue(resultForPlan.any { it.content.contains("propuesta de proyecto") })

        // When
        val resultForLord = noteDao.searchNotes(userId, "Señor").first()

        // Then
        assertEquals(1, resultForLord.size)
        assertEquals("Libros para leer", resultForLord[0].title)
    }

    @Test
    fun countActiveNotes_returnsCorrectCount() = coroutineRule.runTest {
        // Given
        // Insert active notes
        val activeNotes = listOf(
            createTestNote(title = "Active Note 1"),
            createTestNote(title = "Active Note 2"),
            createTestNote(title = "Active Note 3")
        )
        activeNotes.forEach { noteDao.insert(it) }

        // Insert archived note
        val archivedNote = createTestNote(title = "Archived Note", isArchived = true)
        noteDao.insert(archivedNote)

        // Insert deleted note
        val deletedNote = createTestNote(title = "Deleted Note", isDeleted = true)
        noteDao.insert(deletedNote)

        // When
        val count = noteDao.countActiveNotes(userId)

        // Then
        assertEquals(3, count)
    }

    // Función auxiliar para crear una nota de prueba usando TestDataFactory
    private fun createTestNote(
        title: String = "Test Note",
        content: String = "Test content",
        isArchived: Boolean = false,
        isDeleted: Boolean = false
    ): NoteEntity {
        return TestDataFactory.createNoteEntity(
            userId = userId,
            title = title,
            content = content,
            isArchived = isArchived,
            isDeleted = isDeleted
        )
    }
}