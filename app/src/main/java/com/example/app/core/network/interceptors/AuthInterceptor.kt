package com.example.app.core.network.interceptors

import android.util.Log
import com.example.app.BuildConfig
import com.example.app.core.auth.SessionManager
import com.example.app.core.network.models.RefreshTokenRequest
import com.example.app.shared.auth.data.remote.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val authApiService: AuthApi
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val MIN_TIME_BEFORE_EXPIRY = 5 * 60 * 1000L // 5 Minutes
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val requestBuilder = originalRequest.newBuilder()

        // 1. Identify public routes
        val isPublicRoute = path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/refresh")

        // 2. OPTIMISTIC REFRESH CHECK
        if (!isPublicRoute) {
            val session = sessionManager.sessionFlow.value
            val now = System.currentTimeMillis()

            if (session.accessToken != null &&
                session.refreshToken != null &&
                session.expiresAt > 0 &&
                (session.expiresAt - now) < MIN_TIME_BEFORE_EXPIRY
            ) {
                synchronized(this) {
                    val currentSession = sessionManager.sessionFlow.value
                    if ((currentSession.expiresAt - System.currentTimeMillis()) < MIN_TIME_BEFORE_EXPIRY) {
                        try {
                            Log.d(TAG, "Token expiring soon. Attempting optimistic refresh...")
                            val refreshResponse = runBlocking {
                                authApiService.refreshAccessToken(
                                    RefreshTokenRequest(currentSession.refreshToken!!)
                                )
                            }
                            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                                val newCreds = refreshResponse.body()!!
                                sessionManager.updateSession(
                                    accessToken = newCreds.accessToken,
                                    refreshToken = newCreds.refreshToken,
                                    expiresAt = newCreds.expiresAt
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception during refresh", e)
                        }
                    }
                }
            }
        }

        // 3. GET FRESH TOKEN
        val currentToken = sessionManager.sessionFlow.value.accessToken
        if (currentToken != null && !isPublicRoute) {
            requestBuilder.addHeader("Authorization", "Bearer $currentToken")
        }

        // Standard Headers
        requestBuilder.addHeader("X-App-Version", BuildConfig.VERSION_NAME)
        requestBuilder.addHeader("Accept-Language", Locale.getDefault().language)

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // 4. Handle 401 (Unauthorized)
        if (response.code == 401 && !isPublicRoute) {
            runBlocking { sessionManager.logout() }
        }

        return response
    }
}
