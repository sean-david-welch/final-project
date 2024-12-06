package com.budgetai.routes.templates

import com.budgetai.services.BudgetItemService
import com.budgetai.services.BudgetService
import com.budgetai.services.CategoryService
import com.budgetai.services.UserService
import com.budgetai.templates.pages.createDashboardPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRoutes(userService: UserService, budgetItemService: BudgetItemService, budgetService: BudgetService, categoryService: CategoryService) {
    authenticate {
        route("/dashboard") {

            get {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException("User not found")

                val budgetItems = budgetItemService.getBudgetItemsForUser(user.id)
                val budgets = budgetService.getUserBudgets(user.id)
                val categories = categoryService.getCategories()
                call.respondText(text = createDashboardPage(call.templateContext, budgetItems, budgets, categories), contentType = ContentType.Text.Html)
            }
        }
    }
}