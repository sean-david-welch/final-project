package com.budgetai.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowHost(
            host = "127.0.0.1:8080",
            schemes = listOf("http", "https")
        )
        allowHost(
            host = "localhost:8080",
            schemes = listOf("http", "https")
        )
        allowHost(
            host = "0.0.0.0:8080",
            schemes = listOf("http", "https")
        )
        allowHost(
            host = "final-project-production-4c3e.up.railway.app",
            schemes = listOf("http", "https")
        )

        allowCredentials = true
        allowHeaders { true }
    }
}
