package com.budgetai.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// model
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

// dto
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

// serializers