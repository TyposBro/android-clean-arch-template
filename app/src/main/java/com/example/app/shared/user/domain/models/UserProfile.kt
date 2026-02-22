package com.example.app.shared.user.domain.models

data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String?
)
