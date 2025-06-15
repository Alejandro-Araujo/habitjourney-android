package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.model.User
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener los datos del usuario actual desde la fuente de datos.
 *
 * Este caso de uso abstrae la lógica de recuperación de los detalles del perfil
 * del usuario autenticado, delegando la operación al [UserRepository].
 *
 * @property userRepository El repositorio de usuario que proporciona acceso a los datos del usuario.
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Ejecuta la operación para obtener los datos del usuario actualmente autenticado.
     *
     * Al usar el operador `invoke`, puedes llamar a la instancia de `GetUserUseCase`
     * directamente como si fuera una función (por ejemplo, `getUserUseCase()`).
     *
     * @return Un [NetworkResponse] que indica el éxito o error de la operación.
     * Si es exitoso, contendrá el objeto [User] con los datos del perfil.
     */
    suspend operator fun invoke(): NetworkResponse<User> {
        return userRepository.getCurrentUser()
    }
}