package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

fun Route.authenticate(build: Route.() -> Unit) {
    authenticate {
        build()
    }
}

// Role-based authentication middleware
fun Route.requireRole(role: String, build: Route.() -> Unit) {
    authenticate {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val userRole = principal?.payload?.getClaim("role")?.asString()

            if (userRole != role) {
                call.respond(HttpStatusCode.Forbidden, "Insufficient permissions")
                return@intercept
            }
        }
        build()
    }
}

// Example middleware to verify token expiration
fun Route.withValidToken(build: Route.() -> Unit) {
    authenticate {
        intercept(ApplicationCallPipeline.Call) {
            val principal = call.principal<JWTPrincipal>()
            val expiresAt = principal?.expiresAt?.time ?: 0
            val currentTime = System.currentTimeMillis()

            if (expiresAt < currentTime) {
                call.respond(HttpStatusCode.Unauthorized, "Token has expired")
                return@intercept
            }
        }
        build()
    }
}

// Extension function to get userId from JWT token
fun ApplicationCall.getUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
}