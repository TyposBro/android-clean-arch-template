package com.example.app.core.notification

interface PushTokenProvider {
    suspend fun getToken(): String?
}
