package com.example.app.shared.user.domain.repositories

import com.example.app.core.network.models.RepositoryResult
import com.example.app.shared.user.domain.models.UserProfile

interface UserRepository {
    suspend fun getProfile(): RepositoryResult<UserProfile>
}
