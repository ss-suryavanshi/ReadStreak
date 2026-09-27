package com.example.data

import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(private val userDao: UserDao) {
    val loggedInUser: Flow<UserEntity?> = userDao.getLoggedInUser()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun registerUser(
        username: String,
        email: String,
        passwordRaw: String,
        displayName: String
    ): AuthResult {
        val trimmedEmail = email.trim().lowercase()
        val trimmedUsername = username.trim()

        if (trimmedUsername.isBlank()) {
            return AuthResult.Error("Username cannot be empty")
        }
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (passwordRaw.length < 6) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }

        val existingEmail = userDao.getUserByEmail(trimmedEmail)
        if (existingEmail != null) {
            return AuthResult.Error("An account with this email already exists")
        }

        val existingUser = userDao.getUserByUsername(trimmedUsername)
        if (existingUser != null) {
            return AuthResult.Error("Username is already taken")
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(passwordRaw, salt)
        val formattedDate = java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")
        )

        userDao.clearAllLoginStates()

        val newUser = UserEntity(
            username = trimmedUsername,
            email = trimmedEmail,
            passwordHash = passwordHash,
            salt = salt,
            displayName = if (displayName.isNotBlank()) displayName.trim() else trimmedUsername,
            createdAt = formattedDate,
            isLoggedIn = true
        )

        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id.toInt())
        return AuthResult.Success(created)
    }

    suspend fun loginUser(
        emailOrUsername: String,
        passwordRaw: String
    ): AuthResult {
        val input = emailOrUsername.trim().lowercase()
        if (input.isBlank()) {
            return AuthResult.Error("Please enter your email or username")
        }
        if (passwordRaw.isBlank()) {
            return AuthResult.Error("Please enter your password")
        }

        val user = userDao.getUserByEmailOrUsername(input)
            ?: return AuthResult.Error("No local user account found matching credentials")

        val computedHash = SecurityUtils.hashPassword(passwordRaw, user.salt)
        if (computedHash != user.passwordHash) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        userDao.clearAllLoginStates()
        val loggedInUser = user.copy(isLoggedIn = true)
        userDao.updateUser(loggedInUser)

        return AuthResult.Success(loggedInUser)
    }

    suspend fun logout() {
        userDao.clearAllLoginStates()
    }

    suspend fun updateUserProfile(user: UserEntity, newDisplayName: String): UserEntity {
        val updated = user.copy(displayName = newDisplayName)
        userDao.updateUser(updated)
        return updated
    }

    suspend fun updateUserProfilePhoto(user: UserEntity, photoUri: String?): UserEntity {
        val updated = user.copy(profilePhotoUri = photoUri)
        userDao.updateUser(updated)
        return updated
    }
}
