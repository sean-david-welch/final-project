package com.budgetai.routes.templates

import com.budgetai.services.BudgetItemService
import com.budgetai.services.BudgetService
import com.budgetai.services.CategoryService
import com.budgetai.services.UserService
import com.budgetai.templates.pages.createCategoryManagementPage
import com.budgetai.templates.pages.createReportsPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes(userService: UserService, budgetItemService: BudgetItemService, budgetService: BudgetService, categoryService: CategoryService) {
    authenticate {
        // template routes
        route("/reports") {
            get {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException("User not found")

                val budgetItems = budgetItemService.getBudgetItemsForUser(user.id)
                val budgets = budgetService.getUserBudgets(user.id)
                val categories = categoryService.getCategories()
                call.respondText(
                    text = createReportsPage(call.templateContext, budgets, budgetItems, categories), contentType = ContentType.Text.Html
                )
            }
            get("/category-breakdown") {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException("User not found")
                val categories = categoryService.getCategoryByUserId(user.id)
                call.respondText(
                    text = createCategoryManagementPage(call.templateContext, categories, user), contentType = ContentType.Text.Html
                )
            }
            get("/savings-tracking") {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException("User not found")

                val budgetItems = budgetItemService.getBudgetItemsForUser(user.id)
                val budgets = budgetService.getUserBudgets(user.id)
                val categories = categoryService.getCategories()
                call.respondText(
                    text = createReportsPage(call.templateContext, budgets, budgetItems, categories), contentType = ContentType.Text.Html
                )
            }
        }

        // api routes
        route("/api/reports") {
            get("/spending-summary") {  }
            get("/ai-insights") {  }
            get("/category-breakdown") {  }
            get("/savings-tracking") {  }
        }
    }
}