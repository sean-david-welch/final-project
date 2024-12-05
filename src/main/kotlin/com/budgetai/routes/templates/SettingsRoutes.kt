package com.budgetai.routes.templates

import com.budgetai.services.UserService
import com.budgetai.templates.pages.createSettingsPage
import com.budgetai.utils.UserPrincipal
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.settingsRoutes(userService: UserService) {
    authenticate {
        route("/settings") {
            get {
                val principal = call.principal<UserPrincipal>() ?: throw IllegalArgumentException("User not authenticated")
                val user = userService.getUser(principal.id.toInt()) ?: throw IllegalArgumentException("User not found")
                call.respondText(text = createSettingsPage(call.templateContext, user), contentType = ContentType.Text.Html)
            }
        }
    }
}