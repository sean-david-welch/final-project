package com.budgetai.models

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int = 0, val email: String, val name: String, val createdAt: String? = null
)
