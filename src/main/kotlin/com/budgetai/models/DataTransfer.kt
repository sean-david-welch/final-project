package com.budgetai.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserDTO(
    val id: Int = 0,
    val email: String,
    val name: String,
    val createdAt: String? = null
)

@Serializable
data class CategoryDTO(
    val id: Int = 0,
    val name: String,
    val type: CategoryType,
    val description: String? = null,
    val createdAt: String? = null
)

@Serializable
data class BudgetDTO(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val createdAt: String? = null
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