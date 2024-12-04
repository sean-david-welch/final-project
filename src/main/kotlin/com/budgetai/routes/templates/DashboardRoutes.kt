package com.budgetai.routes.templates

import com.budgetai.templates.pages.createDashboardPage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRoutes() {
    authenticate {
        route("/dashboard") {
            get {call.respondText(text = createDashboardPage(call.templateContext), contentType = ContentType.Text.Html)}
        }
    }
}