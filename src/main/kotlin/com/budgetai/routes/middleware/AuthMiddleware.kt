package com.budgetai.routes.middleware

import com.budgetai.utils.AuthenticationException
import com.budgetai.utils.requireRole
import com.budgetai.utils.validateToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Helper function to handle auth failures
private suspend fun ApplicationCall.handleAuthFailure(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: AuthenticationException) {
        when (e.message) {
            "Token has expired" -> respond(HttpStatusCode.Unauthorized, e.message ?: "Token expired")
            else -> respond(HttpStatusCode.Forbidden, e.message ?: "Access denied")
        }
    }
}

// Create plugins for role and token validation
fun createRoleCheckPlugin(role: String) = createRouteScopedPlugin("RoleCheck") {
    onCall { call ->
        call.handleAuthFailure {
            call.principal<JWTPrincipal>()?.requireRole(role)
        }
    }
}

fun createTokenValidationPlugin() = createRouteScopedPlugin("TokenValidation") {
    onCall { call ->
        call.handleAuthFailure {
            call.principal<JWTPrincipal>()?.validateToken()
        }
    }
}

// Route extensions
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