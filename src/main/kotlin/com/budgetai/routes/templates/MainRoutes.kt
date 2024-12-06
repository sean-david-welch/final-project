package com.budgetai.routes.templates

import com.budgetai.services.UserService
import com.budgetai.templates.pages.*
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mainRoutes(userService: UserService) {
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
    get("/admin") {
        if (!call.templateContext.auth.isAdmin) {
            call.respondText(text = create403Page(call.templateContext), contentType = ContentType.Text.Html)
        } else {
            val users = userService.getUsers()
            call.respondText(text = createAdminPage(call.templateContext, users), contentType = ContentType.Text.Html)
        }
    }
    get("/test-error") {
        throw RuntimeException("Test error")
    }
    get("{...}") {
        call.respondText(text = create404Page(call.templateContext), contentType = ContentType.Text.Html)
    }
}