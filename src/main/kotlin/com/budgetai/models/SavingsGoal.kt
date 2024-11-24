package com.budgetai.models

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
