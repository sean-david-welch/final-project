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

fun Route.dashboardRoutes(budgetItemService: BudgetItemService, budgetService: BudgetService, categoryService: CategoryService) {
    authenticate {
        route("/dashboard") {

            get {
                val users = budgetItemService.getUsers()
                val budgets = budgetService.getBudgets()
                val categories = categoryService.getCategories()
                call.respondText(text = createDashboardPage(call.templateContext), contentType = ContentType.Text.Html)
            }
        }
    }
}