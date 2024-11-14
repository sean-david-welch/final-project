package com.budgetai.routes

import com.budgetai.plugins.configureDatabases
import com.budgetai.repositories.UserRepository
import com.budgetai.services.UserService
import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    val database = configureDatabases()
    val userRepository = UserRepository(database)
    val userService = UserService(userRepository)
    routing {
        get("/") {
            call.respondText(
                text = createDashboardPage(),
                contentType = ContentType.Text.Html
            )
        }
        dashboardRoutes()
        userRoutes(userService)
        staticResources("/static", "static")
    }
}