package com.budgetai.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val details: String = null.toString()
)

class ServiceException(message: String) : Exception(message)
