package com.budgetai.routes.templates

import com.budgetai.templates.pages.createSettingsPage
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.settingsRoutes() {
    route("/settings") {
        get("") {
            call.respondText(
                text = createSettingsPage(), contentType = ContentType.Text.Html
            )
        }
    }
}