package com.budgetai.routes.templates

import com.budgetai.models.UserRole
import com.budgetai.routes.middleware.requireRole
import com.budgetai.templates.pages.create404Page
import com.budgetai.templates.pages.createAuthPage
import com.budgetai.templates.pages.createHomePage
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes() {
    get("/") {
        call.respondText(text = createHomePage(call.templateContext), contentType = ContentType.Text.Html)
    }
    get("/auth") {
        if (call.templateContext.auth.isAuthenticated) {
            call.respondRedirect("/dashboard")
        } else {
            call.respondText(text = createAuthPage(call.templateContext), contentType = ContentType.Text.Html)
        }
    }
    get("/test-error") {
        throw RuntimeException("Test error")
    }
    route("/admin") {
        requireRole(UserRole.ADMIN.toString()) {
            get {
                call.respondText(
                    text = "This is an admin route", contentType = ContentType.Text.Plain
                )
            }
        }
    }
    get("{...}") {
        call.respondText(text = create404Page(), contentType = ContentType.Text.Html)
    }
}