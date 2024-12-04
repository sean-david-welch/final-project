package com.budgetai.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.application.*
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
    // Try to validate the JWT token manually
    val jwtCookie = request.cookies["jwt_token"]
    var userPrincipal: UserPrincipal? = null
    var isAuthenticated = false

    jwtCookie?.let { token ->
        try {
            val jwtSecret = application.environment.config.property("jwt.secret").getString()
            val validatedToken = application.validateJwtToken(token, jwtSecret)

            if (validatedToken != null) {
                userPrincipal = UserPrincipal(
                    id = validatedToken.getClaim("id").asString(), email = validatedToken.getClaim("email").asString()
                )
                isAuthenticated = true
                logger.debug("User authenticated - ID: ${userPrincipal?.id}, Email: ${userPrincipal?.email}")
            }
        } catch (e: Exception) {
            logger.error("Error validating JWT token: ${e.message}")
        }
    }

    logger.info("Creating template context for request: ${request.uri}")
    logger.info("JWT cookie present: ${jwtCookie != null}")
    logger.info("Auth state: isAuthenticated=$isAuthenticated")

    return BaseTemplateContext(
        auth = AuthContext(
            user = userPrincipal, isAuthenticated = isAuthenticated
        )
    )
}

// Add this extension function to your Application class
fun Application.validateJwtToken(token: String, secret: String): DecodedJWT? {
    return try {
        val jwtConfig = environment.config.config("jwt")
        val issuer = jwtConfig.property("issuer").getString()
        val audience = jwtConfig.property("audience").getString()

        JWT.require(HMAC256(secret)).withIssuer(issuer).withAudience(audience).build().verify(token)
    } catch (e: Exception) {
        logger.error("JWT validation failed: ${e.message}")
        null
    }
}