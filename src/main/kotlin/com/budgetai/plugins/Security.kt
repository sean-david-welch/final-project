package com.budgetai.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

    logger.info("Configuring security with audience: $jwtAudience, issuer: $jwtIssuer")

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret)).withAudience(jwtAudience).withIssuer(jwtIssuer).build()
            )
            validate { credential ->
                logger.info("Validating token - Audience: ${credential.payload.audience}, Issuer: ${credential.payload.issuer}")
                logger.info("Available claims: ${credential.payload.claims.keys}")

                if (credential.payload.audience.contains(jwtAudience)) {
                    JWTPrincipal(credential.payload).also {
                        logger.info("JWT validation successful - Claims: ${credential.payload.claims}")
                    }
                } else {
                    logger.warn("JWT validation failed - Expected audience: $jwtAudience, Got: ${credential.payload.audience}")
                    null
                }
            }
            authHeader { call ->
                val cookie = call.request.cookies["jwt_token"]
                if (cookie != null) {
                    try {
                        parseAuthorizationHeader("Bearer $cookie").also {
                            logger.info("Successfully parsed auth header from cookie")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to parse auth header from cookie: ${e.message}")
                        null
                    }
                } else {
                    logger.info("No jwt_token cookie found")
                    null
                }
            }
        }
    }
}