package com.alejandro.habitjourney.features.user.domain.usecase


import com.alejandro.habitjourney.core.data.remote.network.NetworkResponse
import com.alejandro.habitjourney.features.user.domain.repository.UserRepository
import javax.inject.Inject

class SendEmailVerificationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): NetworkResponse<Unit> {
        return userRepository.sendEmailVerification()
    }
}