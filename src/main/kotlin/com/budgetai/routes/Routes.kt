package com.budgetai.routes

import com.budgetai.plugins.DatabaseConfig
import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    val database = DatabaseConfig.getDatabase()
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
    }
}