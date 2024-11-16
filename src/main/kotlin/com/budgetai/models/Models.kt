package com.budgetai.models

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import kotlinx.serialization.json.JsonElement

import java.math.BigDecimal

object Users : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Categories : IntIdTable("categories") {
    val name = varchar("name", 50).uniqueIndex()
    val type = enumerationByName("type", 20, CategoryType::class)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

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

object BudgetItems : IntIdTable("budget_items") {
    val budgetId = reference("budget_id", Budgets, onDelete = ReferenceOption.CASCADE)
    val categoryId = reference("category_id", Categories, onDelete = ReferenceOption.RESTRICT)
    val name = varchar("name", 100)
    val amount = decimal("amount", 10, 2)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object SavingsGoals : IntIdTable("savings_goals") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val targetAmount = decimal("target_amount", 10, 2)
    val currentAmount = decimal("current_amount", 10, 2).default(BigDecimal.ZERO)
    val targetDate = date("target_date").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object AiInsights : IntIdTable("ai_insights") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val budgetId = reference("budget_id", Budgets, onDelete = ReferenceOption.CASCADE)
    val budgetItemId = reference("budget_item_id", BudgetItems, onDelete = ReferenceOption.CASCADE).nullable()
    val prompt = text("prompt")
    val response = text("response")
    val type = enumerationByName("type", 20, InsightType::class)
    val sentiment = enumerationByName("sentiment", 20, Sentiment::class).nullable()
    val metadata = json("metadata", Json.Default, JsonElement.serializer()).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}