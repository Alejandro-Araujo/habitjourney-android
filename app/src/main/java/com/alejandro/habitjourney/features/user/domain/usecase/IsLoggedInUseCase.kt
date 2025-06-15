package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para verificar si el usuario está actualmente autenticado.
 *
 * Este caso de uso proporciona un [Flow] reactivo que emite el estado de autenticación del usuario,
 * permitiendo a la UI reaccionar a los cambios de sesión. Delega la verificación al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que proporciona el estado de autenticación.
 */
class IsLoggedInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Recupera el estado de autenticación del usuario.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `IsLoggedInUseCase`
     * directamente como si fuera una función (por ejemplo, `isLoggedInUseCase()`).
     *
     * @return Un [Flow] que emite `true` si el usuario está autenticado, `false` en caso contrario.
     */
    operator fun invoke(): Flow<Boolean> {
        return userRepository.isLoggedIn()
    }
}