package com.budgetai.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


// model
object BudgetItems : IntIdTable("budget_items") {
    val budgetId = reference("budget_id", Budgets, onDelete = ReferenceOption.CASCADE)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.RESTRICT)
    val name = varchar("name", 100)
    val amount = decimal("amount", 10, 2)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

// dto
@Serializable
data class BudgetItemDTO(
    val id: Int = 0,
    val budgetId: Int,
    val categoryId: Int,
    val name: String,
    val amount: Double,
    val createdAt: String? = null
)
