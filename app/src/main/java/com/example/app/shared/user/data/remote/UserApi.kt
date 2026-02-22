package com.example.app.shared.user.data.remote

import com.example.app.shared.user.domain.models.UserProfile
import retrofit2.Response
import retrofit2.http.GET

interface UserApi {
    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfile>
}
