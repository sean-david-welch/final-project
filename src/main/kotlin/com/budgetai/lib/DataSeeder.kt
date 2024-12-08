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

    // Predefined budget templates for more realistic data
    private data class BudgetTemplate(
        val name: String, val description: String, val items: List<BudgetItemTemplate>
    )

    private data class BudgetItemTemplate(
        val categoryName: String, val itemName: String, val amountRange: ClosedRange<Double>
    )

    private val budgetTemplates = listOf(
        BudgetTemplate(
            name = "Monthly Household Budget", description = "Standard monthly household expenses and income", items = listOf(
                BudgetItemTemplate("Salary", "Primary Job Income", 4000.0..8000.0),
                BudgetItemTemplate("Freelance", "Side Gig Income", 500.0..2000.0),
                BudgetItemTemplate("Housing", "Rent/Mortgage", 1200.0..3000.0),
                BudgetItemTemplate("Utilities", "Electricity Bill", 80.0..200.0),
                BudgetItemTemplate("Utilities", "Water Bill", 40.0..100.0),
                BudgetItemTemplate("Utilities", "Internet Service", 50.0..120.0),
                BudgetItemTemplate("Groceries", "Weekly Groceries", 400.0..800.0),
                BudgetItemTemplate("Transportation", "Car Payment", 200.0..500.0),
                BudgetItemTemplate("Transportation", "Fuel", 100.0..300.0),
                BudgetItemTemplate("Healthcare", "Health Insurance", 200.0..600.0),
                BudgetItemTemplate("Entertainment", "Streaming Services", 30.0..100.0),
                BudgetItemTemplate("Entertainment", "Dining Out", 100.0..400.0)
            )
        ), BudgetTemplate(
            name = "Student Budget", description = "Budget tailored for college students", items = listOf(
                BudgetItemTemplate("Salary", "Part-time Job", 800.0..2000.0),
                BudgetItemTemplate("Investment", "Family Support", 500.0..1500.0),
                BudgetItemTemplate("Housing", "Student Housing", 500.0..1200.0),
                BudgetItemTemplate("Utilities", "Combined Utilities", 100.0..250.0),
                BudgetItemTemplate("Groceries", "Food and Supplies", 200.0..400.0),
                BudgetItemTemplate("Education", "Textbooks", 100.0..400.0),
                BudgetItemTemplate("Education", "Course Materials", 50.0..200.0),
                BudgetItemTemplate("Transportation", "Public Transit Pass", 50.0..150.0),
                BudgetItemTemplate("Entertainment", "Student Activities", 50.0..200.0)
            )
        )
    )

    suspend fun seed() {
        // Check if data already exists
        userRepository.findByEmail("user1@example.com")?.let { return }

        // Create or get categories
        val categoryMap = mapOf(
            "Salary" to CategoryType.FIXED,
            "Freelance" to CategoryType.VARIABLE,
            "Investment" to CategoryType.RECURRING,
            "Housing" to CategoryType.FIXED,
            "Utilities" to CategoryType.RECURRING,
            "Groceries" to CategoryType.NECESSARY,
            "Transportation" to CategoryType.FIXED,
            "Healthcare" to CategoryType.NECESSARY,
            "Entertainment" to CategoryType.DISCRETIONARY,
            "Education" to CategoryType.FIXED
        )


        val categoryIds = categoryMap.map { (name, type) ->
            name to (categoryRepository.findByName(name)?.id ?: categoryRepository.create(
                CategoryDTO(
                    name = name, type = type.toString(), description = "Description for $name category"
                )
            ))
        }.toMap()

        // Create users
        val userIds = (1..5).map { index ->
            userRepository.create(
                UserDTO(
                    email = "user$index@example.com", name = "Test User $index", role = UserRole.USER.toString()
                )
            ).also { userId ->
                userRepository.updatePassword(userId, "hashed_password_$index")
                val userCategories = mapOf(
                    "My ${index} Salary" to CategoryType.FIXED,
                    "My ${index} Savings" to CategoryType.FIXED,
                    "My ${index} Rent" to CategoryType.FIXED,
                    "My ${index} Food" to CategoryType.FIXED
                )

                userCategories.forEach { (name, type) ->
                    categoryRepository.create(
                        CategoryDTO(
                            name = name,
                            type = type.toString(),
                            description = "Custom category for user $index",
                            userId = userId  // Associate with specific user
                        )
                    )
                }
            }
        }

        // Create budgets and items for each user
        userIds.forEach { userId ->
            budgetTemplates.forEach { template ->
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                // Calculate start date
                val startDate = LocalDate(now.year, now.monthNumber, 1)

                // Calculate end date (handling year rollover)
                val endDate = if (now.monthNumber == 12) {
                    LocalDate(now.year + 1, 1, 1)
                } else {
                    LocalDate(now.year, now.monthNumber + 1, 1)
                }

                // Calculate totals based on random amounts within ranges
                var totalIncome = 0.0
                var totalExpenses = 0.0
                val itemAmounts = template.items.map { itemTemplate ->
                    val amount = random.nextDouble(
                        itemTemplate.amountRange.start, itemTemplate.amountRange.endInclusive
                    )
                    if (categoryMap[itemTemplate.categoryName] == CategoryType.FIXED) {
                        totalIncome += amount
                    } else {
                        totalExpenses += amount
                    }
                    itemTemplate to amount
                }

                val budgetId = budgetRepository.create(
                    BudgetDTO(
                        userId = userId, name = "${template.name} - ${now.month}", description = template.description,
                        startDate = startDate.toString(), endDate = endDate.toString(), totalIncome = totalIncome,
                        totalExpenses = totalExpenses
                    )
                )

                // Create budget items
                itemAmounts.forEach { (itemTemplate, amount) ->
                    val categoryId = categoryIds[itemTemplate.categoryName] ?: throw IllegalStateException(
                        "Category ${itemTemplate.categoryName} not found"
                    )

                    budgetItemRepository.create(
                        BudgetItemDTO(
                            budgetId = budgetId, categoryId = categoryId, name = itemTemplate.itemName, amount = amount
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
                    userId = userId, name = goalName, description = "Saving up for $goalName", targetAmount = targetAmount,
                    currentAmount = currentAmount, targetDate = targetDate
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
                    userId = userId, budgetId = budgetId, prompt = "Analysis request ${index + 1}",
                    response = "AI generated insight ${index + 1}", type = insightTypes[random.nextInt(insightTypes.size)],
                    sentiment = sentiments[random.nextInt(sentiments.size)], metadata = metadata
                )
            )
        }
    }
}