package com.budgetai.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// models
object Users : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

// DTO
@Serializable
data class UserDTO(
    val id: Int = 0, val email: String, val name: String, val createdAt: String? = null
)

@Serializable
data class UserCreationRequest(
    val email: String, val name: String, val password: String
)

@Serializable
data class UserAuthenticationRequest(
    val email: String, val password: String
)

// Data classes for requests
@Serializable
data class UpdateUserRequest(
    val email: String, val name: String
)

@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String, val newPassword: String
)