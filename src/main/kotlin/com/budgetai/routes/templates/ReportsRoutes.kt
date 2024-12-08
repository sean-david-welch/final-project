package com.budgetai.routes.templates

import com.budgetai.lib.BudgetFormatter
import com.budgetai.services.*
import com.budgetai.templates.pages.createCategoryManagementPage
import com.budgetai.templates.pages.createReportsPage
import com.budgetai.templates.pages.createSavingsManagementPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes(
    userService: UserService, budgetItemService: BudgetItemService, budgetService: BudgetService, categoryService: CategoryService,
    savingsService: SavingsGoalService
) {
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
                val user = call.templateContext.auth.user?.id?.let {
                    userService.getUser(it.toInt())
                } ?: throw IllegalArgumentException("User not found")

                val budgets = budgetService.getUserBudgetsWithItems(user.id)
                val csvFormatter = BudgetFormatter()
                val csvContent = csvFormatter.formatBudgetsToCSV(budgets)

                call.response.headers.apply {
                    append(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                    append(HttpHeaders.ContentDisposition, "attachment; filename=spending-summary.csv")
                    // Add this to prevent caching
                    append(HttpHeaders.CacheControl, "no-cache, no-store, must-revalidate")
                }

                call.respondText(
                    text = csvContent,
                    contentType = ContentType.Text.CSV
                )
            }

            get("/ai-insights") { }
        }
    }
}