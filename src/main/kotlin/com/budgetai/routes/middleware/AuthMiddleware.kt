package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

// Custom exception for auth failures
class AuthenticationException(message: String) : Exception(message)

// Extension function for role validation
fun JWTPrincipal.requireRole(role: String) {
    val userRole = payload.getClaim("role")?.asString()
    if (userRole != role) {
        throw AuthenticationException("Insufficient permissions")
    }
}

// Extension function for token validation
fun JWTPrincipal.validateToken() {
    val expiresAt = expiresAt?.time ?: 0
    if (expiresAt < System.currentTimeMillis()) {
        throw AuthenticationException("Token has expired")
    }
}

// PipelineContext extension to handle auth failures consistently
suspend fun PipelineContext<Unit, ApplicationCall>.handleAuthFailure(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: AuthenticationException) {
        when (e.message) {
            "Token has expired" -> context.respond(HttpStatusCode.Unauthorized, e.message ?: "Token expired")
            else -> context.respond(HttpStatusCode.Forbidden, e.message ?: "Access denied")
        }
        finish()
    }
}

fun ApplicationCall.getUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
}

fun Route.authenticate(build: Route.() -> Unit) {
    authenticate {
        build()
    }
}

fun Route.requireRole(role: String, build: Route.() -> Unit) {
    authenticate {
        install(createRouteScopedPlugin("RoleCheck") {
            onCall { call ->
                val principal = call.principal<JWTPrincipal>()
                val userRole = principal?.payload?.getClaim("role")?.asString()

                if (userRole != role) {
                    call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
                    return@onCall
                }
            }
        })
        build()
    }
}

fun Route.withValidToken(build: Route.() -> Unit) {
    authenticate {
        install(createRouteScopedPlugin("TokenValidation") {
            onCall { call ->
                val principal = call.principal<JWTPrincipal>()
                val expiresAt = principal?.expiresAt?.time ?: 0
                val currentTime = System.currentTimeMillis()

                if (expiresAt < currentTime) {
                    call.respond(HttpStatusCode.Unauthorized, "Token has expired")
                    return@onCall
                }
            }
        })
        build()
    }
}

