package com.alejandro.habitjourney.features.user.domain.usecase

import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsEmailVerifiedUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return userRepository.isEmailVerifiedFlow()
    }
}