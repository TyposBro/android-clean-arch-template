package com.example.app.core.network.models

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(val email: String, val password: String, val name: String)

data class AuthResponse(val accessToken: String, val refreshToken: String, val expiresAt: Long)

data class RefreshTokenRequest(val refreshToken: String)

data class RefreshTokenResponse(val accessToken: String, val refreshToken: String, val expiresAt: Long)
