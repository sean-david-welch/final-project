package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Create plugins for role and token validation
fun createRoleCheckPlugin(role: String) = createRouteScopedPlugin("RoleCheck") {
    onCall { call ->
        handleAuthFailure {
            call.principal<JWTPrincipal>()?.requireRole(role)
        }
    }
}

fun createTokenValidationPlugin() = createRouteScopedPlugin("TokenValidation") {
    onCall { call ->
        handleAuthFailure {
            call.principal<JWTPrincipal>()?.validateToken()
        }
    }
}

// Improved route extensions
fun Route.authenticate(build: Route.() -> Unit) {
    authenticate {
        build()
    }
}

fun Route.requireRole(role: String, build: Route.() -> Unit) {
    authenticate {
        install(createRoleCheckPlugin(role))
        build()
    }
}

fun Route.withValidToken(build: Route.() -> Unit) {
    authenticate {
        install(createTokenValidationPlugin())
        build()
    }
}