package com.budgetai.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Security")

const val TOKEN_EXPIRATION = 60 * 60 * 24

fun Application.configureSecurity(config: ApplicationConfig) {
    val jwtConfig = config.config("jwt")
    val jwtAudience = jwtConfig.property("audience").getString()
    val jwtIssuer = jwtConfig.property("issuer").getString()
    val jwtRealm = jwtConfig.property("realm").getString()
    val jwtSecret = jwtConfig.property("secret").getString()

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                logger.debug("Validating JWT token for audience: {}", credential.payload.audience)
                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload).also {
                        logger.debug("JWT validation successful")
                    }
                } else {
                    logger.warn("JWT validation failed - invalid audience")
                    null
                }
            }
            challenge { _, _ ->
                call.response.status(HttpStatusCode.Unauthorized)
            }
            // Extract JWT from cookie instead of Authorization header
            authHeader { call ->
                call.request.cookies["jwt_token"]?.let { token ->
                    parseAuthorizationHeader("Bearer $token")
                }
            }
        }
    }
}