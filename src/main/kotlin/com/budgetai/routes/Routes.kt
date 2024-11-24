package com.budgetai.routes

import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRoutes(database: Database) {
    routing {
        staticResources("/static", "static")
        get("/") {
            call.respondText(
                text = createDashboardPage(), contentType = ContentType.Text.Html
            )
        }
        userRoutes(database = database)
        budgetRoutes(database = database)
        categoryRoutes(database = database)
        budgetItemRoutes(database = database)
        savingsGoalRoutes(database = database)
        aiInsightRoutes(database = database)
    }
}