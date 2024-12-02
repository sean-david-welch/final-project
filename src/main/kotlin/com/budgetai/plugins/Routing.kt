package com.budgetai.plugins

import com.budgetai.routes.configureRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(config: ApplicationConfig, database: Database? = null) {
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    database?.let { configureRoutes(config, database) } ?: run { configureRoutes(config) }
}
