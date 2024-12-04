package com.budgetai.routes.templates

import com.budgetai.templates.pages.createAuthPage
import com.budgetai.templates.pages.createHomePage
import com.budgetai.utils.createTemplateContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes() {
    get("/") {
        call.respondText(text = createHomePage(), contentType = ContentType.Text.Html)
    }
    get("/auth") {
        val context = call.createTemplateContext()
        if (context.auth.isAuthenticated) {
            call.respondRedirect("/dashboard")
        } else {
            call.respondText(text = createAuthPage(), contentType = ContentType.Text.Html)
        }
    }
}