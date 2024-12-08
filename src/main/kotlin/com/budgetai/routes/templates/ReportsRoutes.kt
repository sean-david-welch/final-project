package com.budgetai.routes.templates

import com.budgetai.lib.AIPromptTemplates
import com.budgetai.lib.BudgetFormatter
import com.budgetai.lib.OpenAi
import com.budgetai.models.*
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
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid user ID")

                val promptType = call.parameters["prompt"]?.let {
                    PromptType.entries.find { type -> type.name.lowercase() == it }
                } ?: throw BadRequestException("Invalid prompt type")

                val budgetId = call.parameters["budget"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid budget ID")

                // 2. Get budget data
                val budget = budgetService.getBudget(budgetId)
                    ?: throw NotFoundException("Budget not found")
                val budgetItems = budgetItemService.getBudgetItems(budgetId)

                // 3. Generate prompt using template
                val prompt = AIPromptTemplates.generatePrompt(budget, budgetItems, promptType)

                // 4. Get AI insight
                val openAi = OpenAi(environment.config)
                val insight = try {
                    openAi.sendMessage(prompt)
                } catch (e: OpenAi.OpenAiException) {
                    throw BadRequestException("Failed to generate insight: ${e.message}")
                }

                // 5. Save and return the insight
                val savedInsight = aiInsightService.createInsight(
                    InsightCreationRequest(
                        userId = userId,
                        budgetId = budgetId,
                        prompt = prompt,
                        type = InsightType.BUDGET_ANALYSIS,
                        sentiment = Sentiment.NEUTRAL,
                        response = insight
                    )
                )

                call.respond(HttpStatusCode.Created, savedInsight)
            }
        }
    }
}