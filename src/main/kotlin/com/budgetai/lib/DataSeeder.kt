package com.budgetai.lib

import com.budgetai.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insertAndGetId
import java.math.BigDecimal
import kotlin.random.Random

class DataSeeder {
    private val random = Random(System.currentTimeMillis())

    fun seed(transaction: Transaction) {
        // Clear existing data
        SchemaUtils.drop(AiInsights, SavingsGoals, BudgetItems, Budgets, Categories, Users)
        SchemaUtils.create(Users, Categories, Budgets, BudgetItems, SavingsGoals, AiInsights)

        // Create users
        val userIds = (1..5).map { createUser(transaction, it) }

        // Create categories (already defined in migration, but we'll get their IDs)
        val categories = mapOf(
            "Salary" to CategoryType.INCOME,
            "Freelance" to CategoryType.INCOME,
            "Investment" to CategoryType.INCOME,
            "Housing" to CategoryType.EXPENSE,
            "Utilities" to CategoryType.EXPENSE,
            "Groceries" to CategoryType.EXPENSE,
            "Transportation" to CategoryType.EXPENSE,
            "Healthcare" to CategoryType.EXPENSE,
            "Entertainment" to CategoryType.EXPENSE,
            "Education" to CategoryType.EXPENSE
        )

        val categoryIds = categories.map { (name, type) ->
            Categories.insertAndGetId {
                it[Categories.name] = name
                it[Categories.type] = type
                it[description] = "Description for $name category"
            }.value
        }

        // Create budgets and items for each user
        userIds.forEach { userId ->
            repeat(2) { budgetNum ->
                val budgetId = createBudget(transaction, userId, budgetNum)

                // Create budget items
                categoryIds.forEach { categoryId ->
                    createBudgetItem(transaction, budgetId, categoryId)
                }

                // Create AI insights for each budget
                createAiInsights(transaction, userId, budgetId)
            }

            // Create savings goals for each user
            createSavingsGoals(transaction, userId)
        }
    }

    private fun createUser(transaction: Transaction, index: Int): Int {
        return Users.insertAndGetId {
            it[email] = "user$index@example.com"
            it[passwordHash] = "hashed_password_$index"
            it[name] = "Test User $index"
        }.value
    }

    private fun createBudget(transaction: Transaction, userId: Int, index: Int): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val startDate = LocalDate(now.year, now.monthNumber, 1)
        val endDate = LocalDate(now.year, now.monthNumber + 1, 1)

        return Budgets.insertAndGetId {
            it[Budgets.userId] = userId
            it[name] = "Budget ${index + 1} for User $userId"
            it[description] = "Monthly budget for ${startDate.month}"
            it[Budgets.startDate] = startDate
            it[Budgets.endDate] = endDate
            it[totalIncome] = BigDecimal(random.nextInt(5000, 10000))
            it[totalExpenses] = BigDecimal(random.nextInt(3000, 7000))
        }.value
    }

    private fun createBudgetItem(transaction: Transaction, budgetId: Int, categoryId: Int) {
        BudgetItems.insert {
            it[BudgetItems.budgetId] = budgetId
            it[BudgetItems.categoryId] = categoryId
            it[name] = "Budget Item for Category $categoryId"
            it[amount] = BigDecimal(random.nextInt(100, 2000))
        }
    }

    private fun createSavingsGoals(transaction: Transaction, userId: Int) {
        val goals = listOf(
            "Emergency Fund" to 10000.0,
            "Vacation" to 5000.0,
            "New Car" to 25000.0
        )

        goals.forEach { (goalName, targetAmount) ->
            SavingsGoals.insert {
                it[SavingsGoals.userId] = userId
                it[name] = goalName
                it[description] = "Saving up for $goalName"
                it[SavingsGoals.targetAmount] = BigDecimal(targetAmount)
                it[currentAmount] = BigDecimal(random.nextDouble(0.0, targetAmount))
                it[targetDate] = LocalDate(2024, random.nextInt(1, 13), 1)
            }
        }
    }

    private fun createAiInsights(transaction: Transaction, userId: Int, budgetId: Int) {
        val insightTypes = InsightType.entries.toTypedArray()
        val sentiments = Sentiment.entries.toTypedArray()

        repeat(3) { index ->
            val metadata = buildJsonObject {
                put("confidence", random.nextDouble(0.7, 1.0))
                put("category", "Analysis $index")
            }

            AiInsights.insert {
                it[AiInsights.userId] = userId
                it[AiInsights.budgetId] = budgetId
                it[prompt] = "Analysis request ${index + 1}"
                it[response] = "AI generated insight ${index + 1}"
                it[type] = insightTypes[random.nextInt(insightTypes.size)]
                it[sentiment] = sentiments[random.nextInt(sentiments.size)]
                it[AiInsights.metadata] = metadata
            }
        }
    }
}
