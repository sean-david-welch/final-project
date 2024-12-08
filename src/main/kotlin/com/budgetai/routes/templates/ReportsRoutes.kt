package com.budgetai.routes.templates

import com.budgetai.lib.BudgetFormatter
import com.budgetai.lib.OpenAi
import com.budgetai.models.AiInsightDTO
import com.budgetai.models.InsightCreationRequest
import com.budgetai.models.PromptType
import com.budgetai.models.Sentiment
import com.budgetai.services.*
import com.budgetai.templates.pages.createCategoryManagementPage
import com.budgetai.templates.pages.createReportsPage
import com.budgetai.templates.pages.createSavingsManagementPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

fun Route.reportRoutes(
    userService: UserService, budgetItemService: BudgetItemService, budgetService: BudgetService, categoryService: CategoryService,
    savingsService: SavingsGoalService, aiInsightService: AiInsightService,
) {
    val logger: Logger = LoggerFactory.getLogger("ReportRoutes")
    authenticate {
        // template routes
        route("/reports") {
            get {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException(
                    "User not found"
                )

                val budgetItems = budgetItemService.getBudgetItemsForUser(user.id)
                val budgets = budgetService.getUserBudgets(user.id)
                val categories = categoryService.getCategories()
                call.respondText(
                    text = createReportsPage(call.templateContext, budgets, budgetItems, categories), contentType = ContentType.Text.Html
                )
            }
            get("/category-breakdown") {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException(
                    "User not found"
                )
                val categories = categoryService.getCategoryByUserId(user.id)
                call.respondText(
                    text = createCategoryManagementPage(call.templateContext, categories), contentType = ContentType.Text.Html
                )
            }
            get("/savings-tracking") {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException(
                    "User not found"
                )
                val savings = savingsService.getUserSavingsGoals(user.id)
                call.respondText(
                    text = createSavingsManagementPage(call.templateContext, savings), contentType = ContentType.Text.Html
                )
            }
        }

        // api routes
        route("/api/reports") {

            get("/spending-summary") {
                logger.info("Received spending summary request")
                try {
                    val user = call.templateContext.auth.user?.id?.let {
                        userService.getUser(it.toInt())
                    } ?: throw IllegalArgumentException("User not found")
                    logger.debug("Found user: ${user.id}")

                    val budgets = budgetService.getUserBudgetsWithItems(user.id)
                    logger.debug("Retrieved ${budgets.size} budgets")

                    val csvFormatter = BudgetFormatter()
                    val csvContent = csvFormatter.formatBudgetsToCSV(budgets)
                    logger.debug("CSV content length: ${csvContent.length}")

                    call.response.headers.apply {
                        append(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                        append(HttpHeaders.ContentDisposition, "attachment; filename=spending-summary.csv")
                        append(HttpHeaders.CacheControl, "no-cache, no-store, must-revalidate")
                    }
                    logger.debug("Set response headers")

                    call.respondText(
                        text = csvContent, contentType = ContentType.Text.CSV
                    )
                    logger.info("Successfully sent CSV response")
                } catch (e: Exception) {
                    logger.error("Error generating spending summary", e)
                    throw e
                }
            }

            post("/ai-insights") {
                // 1. Extract and validate parameters
                val userId = call.parameters["userId"]?.toIntOrNull() ?: throw BadRequestException("Invalid user ID")

                val promptType = call.parameters["prompt"]?.let {
                    PromptType.entries.find { type ->
                        type.name.lowercase() == it
                    }
                } ?: throw BadRequestException("Invalid prompt type")

                val budgetId = call.parameters["budget"]?.toIntOrNull() ?: throw BadRequestException("Invalid budget ID")

                // 2. Get budget data
                val budget = budgetService.getBudget(budgetId) ?: throw NotFoundException("Budget not found")
                val budgetItems = budgetItemService.getBudgetItems(budgetId)

                // 3. Construct prompt based on prompt type
                val prompt = buildString {
                    append("Analyze the following budget data:\n")
                    append("Budget Name: ${budget.name}\n")
                    append("Total Budget: ${budget.totalExpenses}\n")
                    append("Budget Items:\n")
                    budgetItems.forEach { item ->
                        append("- ${item.name}: ${item.amount}\n")
                    }
                    append("\n")

                    append(
                        when (promptType) {
                            PromptType.COST_REDUCTION -> """
                Please analyze this budget for cost reduction opportunities. 
                Identify specific items where costs could be reduced and suggest practical ways to achieve these reductions.
                Format your response in clear, actionable bullet points.
            """.trimIndent()

                            PromptType.PRICE_ALTERNATIVES -> """
                Review the budget items and suggest alternative options or suppliers that could offer better value.
                For each suggestion, explain the potential benefits and savings.
            """.trimIndent()

                            PromptType.SPENDING_PATTERNS -> """
                Analyze the spending patterns in this budget.
                Identify any notable trends, unusual spending, or areas that might need attention.
                Provide specific insights about spending distribution and efficiency.
            """.trimIndent()

                            PromptType.CATEGORY_ANALYSIS -> """
                Perform a detailed category analysis of this budget.
                Group similar items, identify the highest spending categories,
                and suggest any category-specific optimizations.
            """.trimIndent()

                            PromptType.CUSTOM_ANALYSIS -> """
                Provide a comprehensive analysis of this budget.
                Include insights about spending patterns, potential savings,
                and recommendations for better budget management.
            """.trimIndent()
                        }
                    )
                }

                // 4. Send to OpenAI and get response
                val openAi = OpenAi(environment.config)
                val insight = try {
                    openAi.sendMessage(prompt)
                } catch (e: OpenAi.OpenAiException) {
                    throw BadRequestException("Failed to generate insight: ${e.message}")
                }

                // 5. Save the insight
                val savedInsight = aiInsightService.createInsight(
                    InsightCreationRequest(
                        userId = userId,
                        budgetId = budgetId,
                        prompt = prompt,
                        type =,
                        sentiment = Sentiment.NEUTRAL,
                        response = insight,
                    )
                )

                // 6. Return the response
                call.respond(HttpStatusCode.Created, savedInsight)
            }
        }
    }
}