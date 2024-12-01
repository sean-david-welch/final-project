package com.budgetai

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.budgetai.plugins.configureHTTP
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSecurity
import com.budgetai.plugins.configureSerialization
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import java.util.*
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        System.setProperty("config.resource", "development.conf")

        val config = HoconApplicationConfig(ConfigFactory.load("development.conf"))
        application {
            configureSecurity(config)
            configureHTTP()
            configureSerialization()
        }
    }
}

abstract class AuthenticatedTest {
    private val config = HoconApplicationConfig(ConfigFactory.load("development.conf"))

    init {
        System.setProperty("config.resource", "development.conf")
    }

    protected fun ApplicationTestBuilder.configureTestApplication(database: Database) {
        application {
            configureSecurity(config)
            configureSerialization()
            configureRouting(config, database)
        }
    }

    protected fun HttpRequestBuilder.withAuth() {
        cookie("jwt_token", createTestJwtToken())
    }

    private fun createTestJwtToken(): String {
        val jwtConfig = config.config("jwt")
        return JWT.create().withAudience(jwtConfig.property("audience").getString()).withIssuer(jwtConfig.property("issuer").getString())
            .withExpiresAt(Date(System.currentTimeMillis() + 60000)).withClaim("userId", 1).withClaim("role", "user")
            .sign(HMAC256(jwtConfig.property("secret").getString()))
    }
}