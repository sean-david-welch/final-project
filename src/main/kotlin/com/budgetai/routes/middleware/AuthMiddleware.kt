package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Custom exception for auth failures
class AuthenticationException(message: String) : Exception(message)

// Extension function for role validation
fun JWTPrincipal.requireRole(role: String) {
    val userRole = payload.getClaim("role")?.asString()
    if (userRole != role) {
        throw AuthenticationException("Insufficient permissions")
    }
}

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

// Route extensions
fun Route.requireAuth(build: Route.() -> Unit) {
    authenticate {
        build()
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

fun Route.requireRole(role: String, build: Route.() -> Unit) {
    authenticate {
        install(createRoleCheckPlugin(role))
        build()
    }
}