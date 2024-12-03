package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.security.Principal

// Auth context
data class UserPrincipal(val id: String, val email: String) : Principal {
    override fun getName(): String {
        TODO("Not yet implemented")
    }
}

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
