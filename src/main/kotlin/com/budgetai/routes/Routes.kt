package com.budgetai.routes

import com.budgetai.plugins.DatabaseConfig
import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRoutes(database: Database? = null) {
    val db = database ?: DatabaseConfig.getDatabase()

    routing {
        staticResources("/static", "static")
        get("/") {
            call.respondText(
                text = createDashboardPage(), contentType = ContentType.Text.Html
            )
        }
        userRoutes(database = db)
        budgetRoutes(database = db)
        categoryRoutes(database = db)
        budgetItemRoutes(database = db)
        savingsGoalRoutes(database = db)
        aiInsightRoutes(database = db)
    }
}
