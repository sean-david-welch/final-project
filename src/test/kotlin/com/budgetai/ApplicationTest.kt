package com.budgetai

import com.budgetai.plugins.configureHTTP
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSecurity
import com.budgetai.plugins.configureSerialization
import io.ktor.server.testing.*
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureSecurity()
            configureHTTP()
            configureSerialization()
            configureRouting()
        }
    }
}
