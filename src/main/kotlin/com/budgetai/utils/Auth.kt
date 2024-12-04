package com.budgetai.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.auth0.jwt.interfaces.DecodedJWT
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
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
    val jwtCookie = request.cookies["jwt_token"]
    var userPrincipal: UserPrincipal? = null
    var isAuthenticated = false

    jwtCookie?.let { token ->
        try {
            val config = HoconApplicationConfig(ConfigFactory.load())
            val secret = config.property("jwt.secret").getString()
            val validatedToken = validateJwtToken(token, secret, config)

            if (validatedToken != null) {
                userPrincipal = UserPrincipal(
                    id = validatedToken.getClaim("id").asString(), email = validatedToken.getClaim("email").asString()
                )
                isAuthenticated = true
            }
        } catch (e: Exception) {
            logger.error("Error validating JWT token: ${e.message}")
        }
    }

    return BaseTemplateContext(
        auth = AuthContext(
            user = userPrincipal, isAuthenticated = isAuthenticated
        )
    )
}

fun validateJwtToken(token: String, secret: String, config: ApplicationConfig): DecodedJWT? {
    return try {
        val issuer = config.property("jwt.issuer").getString()
        val audience = config.property("jwt.audience").getString()

        JWT.require(HMAC256(secret)).withIssuer(issuer).withAudience(audience).build().verify(token)
    } catch (e: Exception) {
        logger.error("JWT validation failed: ${e.message}")
        null
    }
}