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
    val jwtCookie = request.cookies["jwt_token"]

    logger.info("Creating template context for request: ${request.uri}")
    logger.info("JWT cookie present: ${jwtCookie != null}")
    logger.info("JWT Principal present: ${principal != null}")

    if (jwtCookie != null && principal == null) {
        logger.warn("Cookie exists but JWT validation failed - token might be invalid or expired")
    }

    return BaseTemplateContext(auth = AuthContext(
        user = principal?.let { jwt ->
            try {
                UserPrincipal(
                    id = jwt.payload.getClaim("id").asString(), email = jwt.payload.getClaim("email").asString()
                ).also {
                    logger.debug("User authenticated - ID: ${it.id}, Email: ${it.email}")
                }
            } catch (e: Exception) {
                logger.error("Error extracting user data from JWT - Claims might be missing: ${e.message}")
                logger.debug("Available claims: {}", jwt.payload.claims.keys)
                null
            }
        }, isAuthenticated = principal != null
    ).also {
        logger.info("Auth state: isAuthenticated=${it.isAuthenticated}")
    })
}