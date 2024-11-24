package com.budgetai.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val details: Map<String, String>? = null
)