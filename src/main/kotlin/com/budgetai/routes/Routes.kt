package com.budgetai.routes

import com.budgetai.plugins.DatabaseConfig
import com.budgetai.repositories.*
import com.budgetai.routes.api.*
import com.budgetai.routes.templates.dashboardRoutes
import com.budgetai.services.*
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
    val savingsGoalRepository = SavingsGoalRepository(db)
    val aiInsightsRepository = AiInsightRepository(db)

    // instantiate services
    val userService = UserService(userRepository)
    val budgetService = BudgetService(budgetRepository)
    val categoryService = CategoryService(categoryRepository)
    val budgetItemService = BudgetItemService(budgetItemRepository)
    val savingsGoalService = SavingsGoalService(savingsGoalRepository)
    val aiInsightService = AiInsightService(aiInsightsRepository)


    routing {
        staticResources("/static", "static")

        // Template routes
        dashboardRoutes()

        // API routes
        userRoutes(userService)
        budgetRoutes(budgetService)
        categoryRoutes(categoryService)
        budgetItemRoutes(budgetItemService)
        savingsGoalRoutes(savingsGoalService)
        aiInsightRoutes(aiInsightService)
    }
}
