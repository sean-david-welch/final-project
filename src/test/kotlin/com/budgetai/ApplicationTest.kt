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
    private val testConfig = ConfigFactory.parseString("""
        jwt {
            secret = "your-test-secret-key"
            issuer = "http://0.0.0.0:8080/"
            audience = "http://0.0.0.0:8080/hello"
            realm = "Access to 'hello'"
        }
    """)

    protected fun ApplicationTestBuilder.configureTestApplication(database: Database) {
        application {
            val config = HoconApplicationConfig(testConfig)
            configureSecurity(config)
            configureSerialization()
            configureRouting(config, database)
        }
    }

    protected fun HttpRequestBuilder.withAuth() {
        cookie("jwt_token", createTestJwtToken())
    }

    private fun createTestJwtToken() = JWT.create()
        .withAudience("http://0.0.0.0:8080/hello")
        .withIssuer("http://0.0.0.0:8080/")
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .withClaim("userId", 1)
        .withClaim("role", "user")
        .sign(HMAC256("your-test-secret-key"))
}
