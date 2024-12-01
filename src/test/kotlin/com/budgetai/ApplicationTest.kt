package com.budgetai

import com.budgetai.plugins.configureHTTP
import com.budgetai.plugins.configureSecurity
import com.budgetai.plugins.configureSerialization
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val config = HoconApplicationConfig(ConfigFactory.load())
        application {
            configureSecurity(config, isTest = true)
            configureHTTP()
            configureSerialization()
        }
    }
}
