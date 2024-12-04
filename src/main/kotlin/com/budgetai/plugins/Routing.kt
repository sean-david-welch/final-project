package com.budgetai.plugins

import com.budgetai.routes.configureRoutes
import com.budgetai.templates.pages.create500Page
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
        // Handle specific exceptions
        exception<Throwable> { call, cause ->
            log.error("Unhandled exception occurred", cause)
            call.respondText(
                text = create500Page(),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.InternalServerError
            )
        }

        // Handle specific status codes
        status(HttpStatusCode.InternalServerError) { call, status ->
            call.respondText(
                text = create500Page(),
                contentType = ContentType.Text.Html,
                status = status
            )
        }
    }
    database?.let { configureRoutes(config, database) } ?: run { configureRoutes(config) }
}
