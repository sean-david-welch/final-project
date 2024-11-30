package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

// Custom exception for auth failures
class AuthenticationException(message: String) : Exception(message)

// Extension function for role validation
fun JWTPrincipal.requireRole(role: String) {
    val userRole = payload.getClaim("role")?.asString()
    if (userRole != role) {
        throw AuthenticationException("Insufficient permissions")
    }
}

fun ApplicationCall.getUserId(): String? {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()
}
