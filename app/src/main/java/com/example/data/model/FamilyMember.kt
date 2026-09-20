package com.example.data.model

data class FamilyMember(
    val id: String,
    val name: String,
    val role: UserRole,
    val relation: String,
    val age: Int? = null,
    val avatarEmoji: String,
    val isConnected: Boolean = true
)
