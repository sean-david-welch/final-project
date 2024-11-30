package com.budgetai.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

const val TOKEN_EXPIRATION = 60 * 60 * 24

fun Application.configureSecurity(config: ApplicationConfig) {
    val jwtAudience = config.property("jwt.audience").getString()
    val jwtIssuer = config.property("jwt.issuer").getString()
    val jwtRealm = config.property("jwt.realm").getString()
    val jwtSecret = config.property("jwt.secret").getString()

    authentication {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret)).withAudience(jwtAudience).withIssuer(jwtIssuer).build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.response.status(HttpStatusCode.Unauthorized)
            }
            authHeader { call ->
                call.request.cookies["jwt_token"]?.let { token ->
                    HttpAuthHeader.Single("Bearer", token)
                }
            }
        }
    }
}

