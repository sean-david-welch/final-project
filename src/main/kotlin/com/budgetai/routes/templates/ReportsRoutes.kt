package com.budgetai.routes.templates

import com.budgetai.templates.pages.createReportsPage
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes() {
    route("/reports") {
        get("") {
            call.respondText(
                text = createReportsPage(), contentType = ContentType.Text.Html
            )
        }
    }
}