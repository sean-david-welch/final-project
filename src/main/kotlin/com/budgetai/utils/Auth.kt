package com.budgetai.utils

import com.budgetai.models.UserPrincipal
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

// Auth context
class AuthContext(
    val user: UserPrincipal? = null, val isAuthenticated: Boolean = false
)

// Extension function to easily access auth context
val ApplicationCall.auth: AuthContext
    get() {
        val principal = principal<JWTPrincipal>()
        return if (principal != null) {
            AuthContext(
                user = UserPrincipal(
                    id = principal.payload.getClaim("id").asString(), email = principal.payload.getClaim("email").asString()
                ), isAuthenticated = true
            )
        } else AuthContext()
    }
