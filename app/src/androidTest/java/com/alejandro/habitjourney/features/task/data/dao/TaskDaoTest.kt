package com.alejandro.habitjourney.features.task.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alejandro.habitjourney.core.data.local.database.AppDatabase
import com.alejandro.habitjourney.core.data.local.enums.Priority
import com.alejandro.habitjourney.core.util.TestCoroutineRule
import com.alejandro.habitjourney.core.util.TestDataFactory
import com.alejandro.habitjourney.features.task.data.entity.TaskEntity
import com.alejandro.habitjourney.features.user.data.dao.UserDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
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
        val result = taskDao.getActiveTasksPaged(userId, 10, 0).first { it.id == taskId }
        assertEquals("Updated Task", result.title)
        assertEquals("Updated description", result.description)
    }

    @Test
    fun getActiveTasksPaged_returnsOnlyActiveTasks() = coroutineRule.runTest {
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

        // Insert deleted task
        val deletedTask = createTestTask(title = "Deleted Task", isDeleted = true)
        taskDao.insert(deletedTask)

        // When
        val result = taskDao.getActiveTasksPaged(userId, 10, 0)

        // Then
        assertEquals(3, result.size)
        result.forEach { task ->
            assertFalse(task.isCompleted)
            assertFalse(task.isDeleted)
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

        // Insert deleted task
        val deletedTask = createTestTask(title = "Deleted Task", isDeleted = true)
        taskDao.insert(deletedTask)

        // When
        val result = taskDao.getCompletedTasks(userId, true, 10, 0).first()

        // Then
        assertEquals(3, result.size)
        result.forEach { task ->
            assertTrue(task.isCompleted)
            assertFalse(task.isDeleted)
        }
    }

    @Test
    fun getTasks_returnsAllNonDeletedTasks() = coroutineRule.runTest {
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
            createTestTask(title = "Completed Task 2", isCompleted = true)
        )
        completedTasks.forEach { taskDao.insert(it) }

        // Insert deleted task
        val deletedTask = createTestTask(title = "Deleted Task", isDeleted = true)
        taskDao.insert(deletedTask)

        // When
        val result = taskDao.getTasks(userId, 10, 0).first()

        // Then
        assertEquals(4, result.size)
        result.forEach { task ->
            assertFalse(task.isDeleted)
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

        // When
        val result = taskDao.getOverdueTasks(userId, today, 10, 0).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Overdue Task", result[0].title)
    }

    @Test
    fun setCompleted_marksTaskAsCompleted() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)

        // When
        taskDao.setCompleted(taskId, true)

        // Then
        val result = taskDao.getTasks(userId, 10, 0).first().first { it.id == taskId }
        assertTrue(result.isCompleted)
    }

    @Test
    fun deleteTask_marksTaskAsDeleted() = coroutineRule.runTest {
        // Given
        val task = createTestTask()
        val taskId = taskDao.insert(task)

        // When
        taskDao.deleteTask(taskId)

        // Then
        val allTasks = taskDao.getTasks(userId, 10, 0).first()
        assertTrue(allTasks.none { it.id == taskId })
    }

    // Función auxiliar para crear una tarea de prueba usando TestDataFactory
    private fun createTestTask(
        title: String = "Test Task",
        description: String = "Test description",
        dueDate: LocalDate? = today,
        priority: Priority? = Priority.MEDIUM,
        isCompleted: Boolean = false,
        isDeleted: Boolean = false
    ): TaskEntity {
        return TestDataFactory.createTaskEntity(
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            isCompleted = isCompleted,
            isDeleted = isDeleted
        )
    }
}