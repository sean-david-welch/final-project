package com.budgetai.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.math.BigDecimal

// db model
object Budgets : IntIdTable("budgets") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val totalIncome = decimal("total_income", 10, 2).default(BigDecimal.ZERO)
    val totalExpenses = decimal("total_expenses", 10, 2).default(BigDecimal.ZERO)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

// dto
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

// serializers
@Serializable
data class UpdateBudgetRequest(
    val name: String, val description: String?, val startDate: String?, val endDate: String?
)

@Serializable
data class UpdateBudgetTotalsRequest(
    val totalIncome: Double, val totalExpenses: Double
)