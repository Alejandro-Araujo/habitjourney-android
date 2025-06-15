package com.alejandro.habitjourney.core.data.local.result

/**
 * Una clase sellada (sealed class) genérica para representar los diferentes estados
 * de una operación que puede cargar datos, tener éxito o fallar.
 *
 * @param T El tipo de dato que se espera en caso de éxito.
 */
sealed class Result<out T> { // 'out T' permite la covarianza, bueno para este tipo de clases

    /**
     * Representa el estado de éxito de la operación.
     * @param data Los datos obtenidos.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Representa el estado de error de la operación.
     * @param exception La excepción que causó el error.
     * @param message Un mensaje descriptivo opcional del error.
     */
    data class Error(val exception: Exception, val message: String? = null) : Result<Nothing>()

    /**
     * Representa el estado de carga de la operación.
     * Puede ser un objeto (object) si no necesitas pasar datos durante la carga.
     * O un data object si usas Kotlin 1.9+
     */
    data object Loading : Result<Nothing>()
}