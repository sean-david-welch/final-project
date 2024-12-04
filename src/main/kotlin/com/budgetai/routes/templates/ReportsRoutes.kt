package com.budgetai.routes.templates

import com.budgetai.templates.pages.createReportsPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes() {
    route("/reports") {
        get("") {
            call.respondText(
                text = createReportsPage(call.templateContext), contentType = ContentType.Text.Html
            )
        }
    }
}