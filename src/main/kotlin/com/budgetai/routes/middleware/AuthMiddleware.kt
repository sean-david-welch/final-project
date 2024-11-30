package com.budgetai.routes.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

