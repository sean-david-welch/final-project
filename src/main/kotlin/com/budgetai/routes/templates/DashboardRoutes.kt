package com.budgetai.routes.templates

import com.budgetai.services.BudgetService
import com.budgetai.services.UserService
import com.budgetai.templates.pages.createBudgetManagementPage
import com.budgetai.templates.pages.createDashboardPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRoutes(userService: UserService, budgetService: BudgetService) {
    authenticate {
        route("/dashboard") {
            get {
                call.respondText(text = createDashboardPage(call.templateContext), contentType = ContentType.Text.Html)
            }
            get("/budget-management") {
                val user = call.templateContext.auth.user?.id?.let { userService.getUser(it.toInt()) } ?: throw IllegalArgumentException("User not found")
                val budgets = budgetService.getUserBudgets(user.id)
                call.respondText(text = createBudgetManagementPage(call.templateContext, budgets), contentType = ContentType.Text.Html)
            }
        }
    }
}