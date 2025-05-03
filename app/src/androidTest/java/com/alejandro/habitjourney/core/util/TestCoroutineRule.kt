package com.alejandro.habitjourney.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Regla JUnit para controlar el comportamiento de las corrutinas en tests.
 *
 * Esta regla establece un TestDispatcher como dispatcher principal para todas las corrutinas,
 * lo que permite ejecutar código suspendido de manera sincrónica y controlada en los tests.
 *
 * Uso:
 * ```
 * @get:Rule
 * val coroutineRule = TestCoroutineRule()
 *
 * @Test
 * fun myTest() = coroutineRule.runTest {
 *     // Tu código de test aquí (ejecutado en un TestScope)
 *     val result = myRepository.loadDataSuspending()
 *     assertEquals(expectedResult, result)
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }

    /**
     * Ejecuta una corrutina de test en un TestScope con el TestDispatcher configurado.
     *
     * @param block El bloque de código suspendido a ejecutar.
     */
    fun runTest(block: suspend TestScope.() -> Unit) = kotlinx.coroutines.test.runTest(testDispatcher) {
        block()
    }
}