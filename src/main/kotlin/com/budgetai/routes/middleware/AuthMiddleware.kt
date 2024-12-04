package com.budgetai.routes.middleware

import com.budgetai.templates.pages.create403Page
import com.budgetai.utils.templateContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("AuthMiddleware")

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

    if (userRole == null) {
        throw AuthenticationException("Role claim not found in token")
    }

    if (userRole != role) {
        throw AuthenticationException("Insufficient permissions: required $role, found $userRole")
    }
}

// plugin for middleware
fun createRoleCheckPlugin(role: String) = createRouteScopedPlugin("RoleCheck") {
    onCall { call ->
        val principal = call.principal<JWTPrincipal>()
        logger.info("Checking role: $role for principal: $principal")

        try {
            if (principal == null) {
                logger.error("No principal found in call")
                throw AuthenticationException("No principal found")
            }
            principal.requireRole(role)
        } catch (e: AuthenticationException) {
            call.respondText(
                text = create403Page(call.templateContext), contentType = ContentType.Text.Html, status = HttpStatusCode.Forbidden
            )
            return@onCall
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