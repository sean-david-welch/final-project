package com.budgetai.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// models
object Categories : IntIdTable("categories") {
    val name = varchar("name", 50).uniqueIndex()
    val userId = reference("user_id", Users).nullable()
    val type = varchar("type", 50)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

// DTO
@Serializable
data class CategoryDTO(
    val id: Int = 0, val userId: Int? = null, val name: String, val type: String = CategoryType.FIXED.toString(), val description: String? = null,
    val createdAt: String? = null
)

// Serializers
@Serializable
data class UpdateCategoryRequest(
    val name: String, val type: String = CategoryType.FIXED.toString(), val description: String?
)

@Serializable
data class UpdateCategoryTypeRequest(
    val type: String,
)

@Serializable
data class CategoryCreationRequest(
    val userId: Int? = null, val name: String, val type: String = CategoryType.FIXED.toString(), val description: String? = null
)