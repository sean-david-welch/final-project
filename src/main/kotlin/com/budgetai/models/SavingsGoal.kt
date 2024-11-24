package com.budgetai.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.math.BigDecimal


object SavingsGoals : IntIdTable("savings_goals") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val targetAmount = decimal("target_amount", 10, 2)
    val currentAmount = decimal("current_amount", 10, 2).default(BigDecimal.ZERO)
    val targetDate = date("target_date").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

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


// Serializers
@Serializable
data class SavingsGoalCreationRequest(
    val userId: Int,
    val name: String,
    val description: String? = null,
    val targetAmount: Double,
    val initialAmount: Double = 0.0,
    val targetDate: String? = null
)

@Serializable
data class SavingsGoalUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val targetAmount: Double? = null,
    val targetDate: String? = null
)

@Serializable
data class GoalProgress(
    val currentAmount: Double,
    val targetAmount: Double,
    val percentageComplete: Double,
    val remainingAmount: Double,
    val isOnTrack: Boolean,
    val requiredDailySavings: Double
)

