package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.security.Principal

data class BaseTemplateContext(
    val auth: AuthContext,
)

data class UserPrincipal(
    val id: String,
    val email: String
)

data class AuthContext(
    val user: UserPrincipal? = null,
    val isAuthenticated: Boolean = false
)

fun ApplicationCall.createTemplateContext(): BaseTemplateContext {
    return BaseTemplateContext(
        auth = AuthContext(
            user = principal<JWTPrincipal>()?.let { principal ->
                UserPrincipal(
                    id = principal.payload.getClaim("id").asString(),
                    email = principal.payload.getClaim("email").asString()
                )
            },
            isAuthenticated = principal<JWTPrincipal>() != null
        )
    )
}
