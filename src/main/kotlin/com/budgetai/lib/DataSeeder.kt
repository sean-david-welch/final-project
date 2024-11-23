package com.budgetai.lib

import com.budgetai.models.*
import com.budgetai.repositories.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.Database
import kotlin.random.Random

class DataSeeder(database: Database) {
    private val userRepository = UserRepository(database)
    private val categoryRepository = CategoryRepository(database)
    private val budgetRepository = BudgetRepository(database)
    private val budgetItemRepository = BudgetItemRepository(database)
    private val savingsGoalRepository = SavingsGoalRepository(database)
    private val aiInsightRepository = AiInsightRepository(database)

    private val random = Random(System.currentTimeMillis())

    suspend fun seed() {
        // create users
        userRepository.findByEmail("user1@example.com")?.let { return }
        val userIds = (1..5).map { index ->
            userRepository.create(
                UserDTO(
                    email = "user$index@example.com",
                    name = "Test User $index"
                )
            ).also { userId ->
                userRepository.updatePassword(userId, "hashed_password_$index")
            }
        }

        // Create categories
        val categoryMap = mapOf(
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

        val categoryIds = categoryMap.map { (name, type) ->
            categoryRepository.create(
                CategoryDTO(
                    name = name, type = type, description = "Description for $name category"
                )
            )
        }

        // Create budgets and items for each user
        userIds.forEach { userId ->
            repeat(2) { budgetNum ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                val startDate = LocalDate(now.year, now.monthNumber, 1).toString()
                val endDate = LocalDate(now.year, now.monthNumber + 1, 1).toString()

                val budgetId = budgetRepository.create(
                    BudgetDTO(
                        userId = userId,
                        name = "Budget ${budgetNum + 1} for User $userId",
                        description = "Monthly budget for ${now.month}",
                        startDate = startDate,
                        endDate = endDate,
                        totalIncome = random.nextInt(5000, 10000).toDouble(),
                        totalExpenses = random.nextInt(3000, 7000).toDouble()
                    )
                )

                // Create budget items
                categoryIds.forEach { categoryId ->
                    budgetItemRepository.create(
                        BudgetItemDTO(
                            budgetId = budgetId,
                            categoryId = categoryId,
                            name = "Budget Item for Category $categoryId",
                            amount = random.nextInt(100, 2000).toDouble()
                        )
                    )
                }

                // Create AI insights
                createAiInsights(userId, budgetId)
            }

            // Create savings goals
            createSavingsGoals(userId)
        }
    }

    private suspend fun createSavingsGoals(userId: Int) {
        val goals = listOf(
            "Emergency Fund" to 10000.0, "Vacation" to 5000.0, "New Car" to 25000.0
        )

        goals.forEach { (goalName, targetAmount) ->
            val currentAmount = random.nextDouble(0.0, targetAmount)
            val targetDate = LocalDate(2024, random.nextInt(1, 13), 1).toString()

            savingsGoalRepository.create(
                SavingsGoalDTO(
                    userId = userId,
                    name = goalName,
                    description = "Saving up for $goalName",
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate
                )
            )
        }
    }

    private suspend fun createAiInsights(userId: Int, budgetId: Int) {
        val insightTypes = InsightType.entries.toTypedArray()
        val sentiments = Sentiment.entries.toTypedArray()

        repeat(3) { index ->
            val metadata = buildJsonObject {
                put("confidence", random.nextDouble(0.7, 1.0))
                put("category", "Analysis $index")
            }

            aiInsightRepository.create(
                AiInsightDTO(
                    userId = userId,
                    budgetId = budgetId,
                    prompt = "Analysis request ${index + 1}",
                    response = "AI generated insight ${index + 1}",
                    type = insightTypes[random.nextInt(insightTypes.size)],
                    sentiment = sentiments[random.nextInt(sentiments.size)],
                    metadata = metadata
                )
            )
        }
    }
}