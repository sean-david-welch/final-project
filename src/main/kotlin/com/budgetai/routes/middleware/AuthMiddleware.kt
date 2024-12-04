package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("AuthMiddleware")

// Custom exception for auth failures
// Helper function to handle auth failures

class AuthenticationException(message: String) : Exception(message)
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

// Custom middlware - Require role
fun JWTPrincipal.requireRole(role: String) {
    val userRole = payload.getClaim("role")?.asString()
    logger.info("Required role: $role, User role: $userRole")
    if (userRole != role) {
        throw AuthenticationException("Insufficient permissions")
    }
}

// plugin for middleware
fun createRoleCheckPlugin(role: String) = createRouteScopedPlugin("RoleCheck") {
    onCall { call ->
        try {
            call.principal<JWTPrincipal>()?.requireRole(role)
                ?: throw AuthenticationException("No principal found")
        } catch (e: AuthenticationException) {
            call.respond(HttpStatusCode.Forbidden, "Access denied: ${e.message}")
            throw e
        }
    }
}

// call in route
fun Route.requireRole(role: String, build: Route.() -> Unit) {
    authenticate {
        install(createRoleCheckPlugin(role))
        build()
    }
}