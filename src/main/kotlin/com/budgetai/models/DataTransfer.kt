package com.budgetai.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.math.BigDecimal

@Serializable
data class UserDTO(
    val id: Int = 0,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime? = null
)

@Serializable
data class CategoryDTO(
    val id: Int = 0,
    val name: String,
    val type: CategoryType,
    val description: String? = null,
    val createdAt: LocalDateTime? = null
)

@Serializable
data class BudgetDTO(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    @Contextual val totalIncome: BigDecimal = BigDecimal.ZERO,
    @Contextual val totalExpenses: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null
)

@Serializable
data class BudgetItemDTO(
    val id: Int = 0,
    val budgetId: Int,
    val categoryId: Int,
    val name: String,
    @Contextual val amount: BigDecimal,
    val createdAt: LocalDateTime? = null
)

@Serializable
data class SavingsGoalDTO(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    @Contextual val targetAmount: BigDecimal,
    @Contextual val currentAmount: BigDecimal = BigDecimal.ZERO,
    val targetDate: LocalDate? = null,
    val createdAt: LocalDateTime? = null
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
    val createdAt: LocalDateTime? = null
)