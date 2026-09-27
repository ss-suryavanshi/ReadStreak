package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true),
        Index(value = ["isLoggedIn"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val displayName: String,
    val createdAt: String,
    val isLoggedIn: Boolean = false,
    val profilePhotoUri: String? = null
)
