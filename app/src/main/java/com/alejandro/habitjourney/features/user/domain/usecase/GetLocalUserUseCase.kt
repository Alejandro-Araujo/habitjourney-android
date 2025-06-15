package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el usuario localmente almacenado.
 *
 * Este caso de uso proporciona una forma de acceder al objeto [User]
 * que está guardado en la base de datos o preferencias locales de la aplicación.
 * Emite los datos como un [Flow], permitiendo una observación reactiva.
 *
 * @property userRepository El repositorio de usuario que proporciona acceso a los datos locales.
 */
class GetLocalUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Recupera el usuario localmente almacenado.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `GetLocalUserUseCase`
     * directamente como si fuera una función (por ejemplo, `getLocalUserUseCase()`).
     *
     * @return Un [Flow] que emite el objeto [User] local o `null` si no hay un usuario local.
     */
    operator fun invoke(): Flow<User?> {
        return userRepository.getLocalUser()
    }
}