package com.budgetai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserDTO(
    val id: Int = 0, val email: String, val name: String, val createdAt: String? = null
)



@Serializable
data class BudgetItemDTO(
    val id: Int = 0,
    val budgetId: Int,
    val categoryId: Int,
    val name: String,
    val amount: Double,
    val createdAt: String? = null
)

@Serializable
data class SavingsGoalDTO(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AiInsightDTO(
    val id: Int = 0,
    val userId: Int,
    val budgetId: Int,
    val budgetItemId: Int? = null,
    val prompt: String,
    val response: String,
    val type: InsightType,
    val sentiment: Sentiment? = null,
    val metadata: JsonElement? = null,
    val createdAt: String? = null
)