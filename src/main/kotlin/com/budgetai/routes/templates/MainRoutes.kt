package com.budgetai.routes.templates

import com.budgetai.services.BudgetService
import com.budgetai.services.CategoryService
import com.budgetai.services.UserService
import com.budgetai.templates.pages.*
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes(userService: UserService, budgetService: BudgetService, categoryService: CategoryService) {
    get("/") {
        call.respondText(text = createHomePage(call.templateContext), contentType = ContentType.Text.Html)
    }
    get("/auth") {
        if (call.templateContext.auth.isAuthenticated) {
            call.respondRedirect("/dashboard")
        } else {
            call.respondText(text = createAuthPage(call.templateContext), contentType = ContentType.Text.Html)
        }
    }
    get("/admin") {
        if (!call.templateContext.auth.isAdmin) {
            call.respondText(text = create403Page(call.templateContext), contentType = ContentType.Text.Html)
        } else {
            val users = userService.getUsers()
            val budgets = budgetService.getBudgets()
            val categories = categoryService.getCategories()
            call.respondText(text = createAdminPage(call.templateContext, users, budgets, categories), contentType = ContentType.Text.Html)
        }
    }
    get("/admin/user-management") {
        if (!call.templateContext.auth.isAdmin) {
            call.respondText(text = create403Page(call.templateContext), contentType = ContentType.Text.Html)
        } else {
            val users = userService.getUsers()
            call.respondText(text = createUserPage(call.templateContext, users), contentType = ContentType.Text.Html)
        }
    }
    get("/admin/budget-management") {
        if (!call.templateContext.auth.isAdmin) {
            call.respondText(text = create403Page(call.templateContext), contentType = ContentType.Text.Html)
        } else {
            val budgets = budgetService.getBudgets()
            call.respondText(text = createBudgetManagementPage(call.templateContext, budgets), contentType = ContentType.Text.Html)
        }
    }
    get("/test-error") {
        throw RuntimeException("Test error")
    }
    get("{...}") {
        call.respondText(text = create404Page(call.templateContext), contentType = ContentType.Text.Html)
    }
}