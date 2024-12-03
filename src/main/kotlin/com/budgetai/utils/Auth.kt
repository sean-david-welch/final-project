package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AuthContext")

data class BaseTemplateContext(
    val auth: AuthContext,
)

data class UserPrincipal(
    val id: String, val email: String
)

data class AuthContext(
    val user: UserPrincipal? = null, val isAuthenticated: Boolean = false
)

fun ApplicationCall.createTemplateContext(): BaseTemplateContext {
    val principal = principal<JWTPrincipal>()

    // Log the authentication attempt
    logger.info("Creating template context for request: ${request.uri}")
    logger.debug("JWT Principal present: ${principal != null}")

    return BaseTemplateContext(auth = AuthContext(
        user = principal?.let { jwt ->
            try {
                UserPrincipal(
                    id = jwt.payload.getClaim("id").asString(), email = jwt.payload.getClaim("email").asString()
                ).also {
                    logger.debug("User authenticated - ID: ${it.id}, Email: ${it.email}")
                }
            } catch (e: Exception) {
                logger.error("Error extracting user data from JWT: ${e.message}")
                null
            }
        }, isAuthenticated = principal != null
    ).also {
        logger.info("Auth state: isAuthenticated=${it.isAuthenticated}")
    })
}