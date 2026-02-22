package com.example.app.shared.user.data.repositories

import com.example.app.core.network.models.RepositoryResult
import com.example.app.core.network.models.safeApiCall
import com.example.app.shared.user.data.remote.UserApi
import com.example.app.shared.user.domain.models.UserProfile
import com.example.app.shared.user.domain.repositories.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getProfile(): RepositoryResult<UserProfile> {
        return safeApiCall { userApi.getProfile() }
    }
}
