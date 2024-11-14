package com.budgetai.routes

import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    routing {
        get("/") {
            call.respondText(
                text = createDashboardPage(),
                contentType = ContentType.Text.Html
            )
        }
        dashboardRoutes()
        staticResources("/static", "static")
    }
}