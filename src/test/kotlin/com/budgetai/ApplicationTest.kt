package com.budgetai

import com.budgetai.plugins.configureRouting
import io.ktor.server.testing.*
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
    }
}
