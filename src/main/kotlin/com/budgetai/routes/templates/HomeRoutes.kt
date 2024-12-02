package com.budgetai.routes.templates

import com.budgetai.templates.pages.createHomePage
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes() {
    get("/") {
        call.respondText(
            text = createHomePage(), contentType = ContentType.Text.Html
        )
    }
}