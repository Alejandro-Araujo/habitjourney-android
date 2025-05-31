package com.alejandro.habitjourney.features.task.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.user.data.local.dao.UserDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var userDao: UserDao
    private var userId: Long = 0

    private val today = TestDataFactory.TODAY

    @Before
    fun setupDatabase() {
        // Crear una instancia en memoria de la base de datos para testing
        database = TestDataFactory.createInMemoryDatabase()

        taskDao = database.taskDao()
        userDao = database.userDao()

        // Crear un usuario para las pruebas
        coroutineRule.runTest {
            val user = TestDataFactory.createUserEntity(
                id = 1L,
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
    fun insertTask_returnsId() = coroutineRule.runTest {
        // Given
        val task = createTestTask()

        // When
        val taskId = taskDao.insert(task)

        // Then
        assertTrue(taskId > 0)
    }

    @Test
    fun updateTask_updatesCorrectTask() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)
        val updatedTask = task.copy(
            id = taskId,
            title = "Updated Task",
            description = "Updated description"
        )

        // When
        taskDao.update(updatedTask)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNotNull(result)
        assertEquals("Updated Task", result!!.title)
        assertEquals("Updated description", result.description)
    }

    @Test
    fun getActiveTasks_returnsOnlyActiveTasks() = coroutineRule.runTest {
        // Given
        // Insert active tasks
        val activeTasks = listOf(
            createTestTask(title = "Active Task 1"),
            createTestTask(title = "Active Task 2"),
            createTestTask(title = "Active Task 3")
        )
        activeTasks.forEach { taskDao.insert(it) }

        // Insert completed task
        val completedTask = createTestTask(title = "Completed Task", isCompleted = true)
        taskDao.insert(completedTask)

        // Insert archived task
        val archivedTask = createTestTask(title = "Archived Task", isArchived = true)
        taskDao.insert(archivedTask)

        // When
        val result = taskDao.getActiveTasks(userId).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { task ->
            assertFalse(task.isCompleted)
            assertFalse(task.isArchived)
        }
    }

    @Test
    fun getCompletedTasks_returnsOnlyCompletedTasks() = coroutineRule.runTest {
        // Given
        // Insert active tasks
        val activeTasks = listOf(
            createTestTask(title = "Active Task 1"),
            createTestTask(title = "Active Task 2")
        )
        activeTasks.forEach { taskDao.insert(it) }

        // Insert completed tasks
        val completedTasks = listOf(
            createTestTask(title = "Completed Task 1", isCompleted = true),
            createTestTask(title = "Completed Task 2", isCompleted = true),
            createTestTask(title = "Completed Task 3", isCompleted = true)
        )
        completedTasks.forEach { taskDao.insert(it) }

        // Insert archived task
        val archivedTask = createTestTask(title = "Archived Task", isArchived = true)
        taskDao.insert(archivedTask)

        // When
        val result = taskDao.getCompletedTasks(userId).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { task ->
            assertTrue(task.isCompleted)
            assertFalse(task.isArchived)
        }
    }

    @Test
    fun getArchivedTasks_returnsOnlyArchivedTasks() = coroutineRule.runTest {
        // Given
        val activeTasks = listOf(
            createTestTask(title = "Active Task 1"),
            createTestTask(title = "Active Task 2")
        )
        activeTasks.forEach { taskDao.insert(it) }

        val archivedTasks = listOf(
            createTestTask(title = "Archived Task 1", isArchived = true),
            createTestTask(title = "Archived Task 2", isArchived = true),
            createTestTask(title = "Archived Task 3", isArchived = true, isCompleted = true)
        )
        archivedTasks.forEach { taskDao.insert(it) }

        // When
        val result = taskDao.getArchivedTasks(userId).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { task ->
            assertTrue(task.isArchived)
        }
    }

    @Test
    fun getAllTasks_returnsAllNonArchivedTasks() = coroutineRule.runTest {
        // Given
        val activeTasks = listOf(
            createTestTask(title = "Active Task 1"),
            createTestTask(title = "Active Task 2")
        )
        activeTasks.forEach { taskDao.insert(it) }

        val completedTasks = listOf(
            createTestTask(title = "Completed Task 1", isCompleted = true),
            createTestTask(title = "Completed Task 2", isCompleted = true)
        )
        completedTasks.forEach { taskDao.insert(it) }

        val archivedTask = createTestTask(title = "Archived Task", isArchived = true)
        taskDao.insert(archivedTask)

        // When
        val result = taskDao.getAllTasks(userId).first()

        // Then
        assertEquals(4, result.size) // 2 active + 2 completed (no archived)
        result.forEach { task ->
            assertFalse(task.isArchived)
        }
    }

    @Test
    fun getOverdueTasks_returnsOnlyOverdueTasks() = coroutineRule.runTest {
        // Given
        val yesterday = today - DatePeriod(days = 1)
        val tomorrow = today + DatePeriod(days = 1)

        // Insert tasks with different due dates
        val overdueTask = createTestTask(title = "Overdue Task", dueDate = yesterday)
        taskDao.insert(overdueTask)

        val futureTask = createTestTask(title = "Future Task", dueDate = tomorrow)
        taskDao.insert(futureTask)

        val completedOverdueTask = createTestTask(
            title = "Completed Overdue Task",
            dueDate = yesterday,
            isCompleted = true
        )
        taskDao.insert(completedOverdueTask)

        val archivedOverdueTask = createTestTask(
            title = "Archived Overdue Task",
            dueDate = yesterday,
            isArchived = true
        )
        taskDao.insert(archivedOverdueTask)

        // When
        val result = taskDao.getOverdueTasks(userId, today).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Overdue Task", result[0].title)
    }

    @Test
    fun getTaskById_returnsTaskIncludingArchived() = coroutineRule.runTest {
        // Given
        val activeTask = createTestTask(title = "Active Task")
        val activeTaskId = taskDao.insert(activeTask)

        val archivedTask = createTestTask(title = "Archived Task", isArchived = true)
        val archivedTaskId = taskDao.insert(archivedTask)

        // When & Then - Active task
        val activeResult = taskDao.getTaskById(activeTaskId).first()
        assertNotNull(activeResult)
        assertEquals("Active Task", activeResult!!.title)

        // When & Then - Archived task (should be found now)
        val archivedResult = taskDao.getTaskById(archivedTaskId).first()
        assertNotNull(archivedResult)
        assertEquals("Archived Task", archivedResult!!.title)
        assertTrue(archivedResult.isArchived)
    }

    @Test
    fun searchTasks_returnsMatchingTasks() = coroutineRule.runTest {
        // Given
        val tasks = listOf(
            createTestTask(title = "Important meeting"),
            createTestTask(title = "Buy groceries"),
            createTestTask(title = "Important project", description = "Very important"),
            createTestTask(title = "Regular task")
        )
        tasks.forEach { taskDao.insert(it) }

        val archivedTask = createTestTask(title = "Important archived", isArchived = true)
        taskDao.insert(archivedTask)

        // When
        val result = taskDao.searchTasks(userId, "important").first()

        // Then
        assertEquals(2, result.size) // No debe incluir la archivada
        assertTrue(result.any { it.title.contains("Important meeting") })
        assertTrue(result.any { it.title.contains("Important project") })
    }

    @Test
    fun setCompleted_marksTaskAsCompleted() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)

        // When
        taskDao.setCompleted(taskId, true, today)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNotNull(result)
        assertTrue(result!!.isCompleted)
        assertEquals(today, result.completionDate)
    }

    @Test
    fun archiveTask_marksTaskAsArchived() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)

        // When
        taskDao.archiveTask(taskId)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNotNull(result)
        assertTrue(result!!.isArchived)
    }

    @Test
    fun unarchiveTask_marksTaskAsNotArchived() = coroutineRule.runTest {
        // Given
        val task = createTestTask(isArchived = true)
        val taskId = taskDao.insert(task)

        // When
        taskDao.unarchiveTask(taskId)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNotNull(result)
        assertFalse(result!!.isArchived)
    }

    @Test
    fun deleteTask_removesTaskCompletely() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)

        // When
        taskDao.deleteTask(taskId)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNull(result) // La tarea debe estar completamente eliminada
    }

    @Test
    fun insertTaskWithReminder_savesReminderCorrectly() = coroutineRule.runTest {
        // Given
        val reminderDateTime = LocalDateTime(today.year, today.month, today.dayOfMonth, 14, 30)
        val task = createTestTask(
            title = "Task with reminder",
            reminderDateTime = reminderDateTime,
            isReminderSet = true
        )

        // When
        val taskId = taskDao.insert(task)

        // Then
        val result = taskDao.getTaskById(taskId).first()
        assertNotNull(result)
        assertTrue(result!!.isReminderSet)
        assertEquals(reminderDateTime, result.reminderDateTime)
    }

    private fun createTestTask(
        title: String = "Test Task",
        description: String? = "Test description",
        dueDate: LocalDate? = today,
        priority: Priority? = Priority.MEDIUM,
        isCompleted: Boolean = false,
        isArchived: Boolean = false,
        reminderDateTime: LocalDateTime? = null,
        isReminderSet: Boolean = false
    ): TaskEntity {
        return TaskEntity(
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            isCompleted = isCompleted,
            completionDate = if (isCompleted) today else null,
            isArchived = isArchived,
            createdAt = System.currentTimeMillis(),
            reminderDateTime = reminderDateTime,
            isReminderSet = isReminderSet
        )
    }
}