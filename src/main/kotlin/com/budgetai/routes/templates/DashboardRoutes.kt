package com.budgetai.routes.templates

import com.budgetai.templates.pages.createDashboardPage
import com.budgetai.utils.createTemplateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dashboardRoutes() {
    route("/dashboard") {
        authenticate {
            get {
                val context = call.createTemplateContext()
                call.respondText(text = createDashboardPage(context), contentType = ContentType.Text.Html)
            }
        }
    }
}