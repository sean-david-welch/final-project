package com.budgetai.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
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

fun ApplicationCall.getUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
}
