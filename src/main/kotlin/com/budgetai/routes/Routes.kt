package com.budgetai.routes

import com.budgetai.plugins.DatabaseConfig
import com.budgetai.repositories.BudgetItemRepository
import com.budgetai.repositories.BudgetRepository
import com.budgetai.repositories.CategoryRepository
import com.budgetai.repositories.UserRepository
import com.budgetai.services.BudgetItemService
import com.budgetai.services.BudgetService
import com.budgetai.services.CategoryService
import com.budgetai.services.UserService
import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRoutes(database: Database? = null) {
    // database instantiation if null
    val db = database ?: DatabaseConfig.getDatabase()

    // instantiate repositories
    val userRepository = UserRepository(db)
    val budgetRepository = BudgetRepository(db)
    val categoryRepository = CategoryRepository(db)
    val budgetItemRepository = BudgetItemRepository(db)

    // instantiate services
    val userService = UserService(userRepository)
    val budgetService = BudgetService(budgetRepository)
    val categoryService = CategoryService(categoryRepository)
    val budgetItemService = BudgetItemService(budgetItemRepository)

    routing {
        staticResources("/static", "static")
        get("/") {
            call.respondText(
                text = createDashboardPage(), contentType = ContentType.Text.Html
            )
        }
        userRoutes(userService)
        budgetRoutes(budgetService)
        categoryRoutes(categoryService)
        budgetItemRoutes(budgetItemService)
        savingsGoalRoutes(database = db)
        aiInsightRoutes(database = db)
    }
}
