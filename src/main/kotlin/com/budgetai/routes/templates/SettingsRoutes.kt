package com.budgetai.routes.templates

import com.budgetai.templates.pages.createDashboardPage
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.settingsRoutes() {
    route("/settings") {
        get("") {
            call.respondText(
                text = createDashboardPage(), contentType = ContentType.Text.Html
            )
        }
    }
}