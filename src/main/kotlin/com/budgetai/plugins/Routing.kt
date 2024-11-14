package com.budgetai.plugins

import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.webjars.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    install(Webjars) {
        path = "/webjars"
    }
    routing {
        get("/") {
            call.respondText(
                text = createDashboardPage(),
                contentType = ContentType.Text.Html
            )
        }
        dashboardRoutes()
        staticResources("/static", "static")
        get("/webjars") {
            call.respondText("<script src='/webjars/jquery/jquery.js'></script>", ContentType.Text.Html)
        }
    }
}

fun Route.dashboardRoutes() {
    route("/dashboard") {
        get {
            call.respondText(
                text = createDashboardPage(),
                contentType = ContentType.Text.Html
            )
        }
    }
}