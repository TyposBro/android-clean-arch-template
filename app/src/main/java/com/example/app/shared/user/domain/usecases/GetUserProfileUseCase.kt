package com.example.app.shared.user.domain.usecases

import com.example.app.core.network.models.RepositoryResult
import com.example.app.shared.user.domain.models.UserProfile
import com.example.app.shared.user.domain.repositories.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): RepositoryResult<UserProfile> {
        return userRepository.getProfile()
    }
}
