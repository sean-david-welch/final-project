package com.budgetai

import com.budgetai.lib.DataSeeder
import com.budgetai.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseConfig.initialize()
    try {
        val isDevelopment = environment.config.property("ktor.development").getString().toBoolean()
        if (isDevelopment) {
            log.info("Running in development mode - seeding database...")
            try {
                transaction {
                    DataSeeder().seed(this)
                }
                log.info("Database seeding completed successfully")
            } catch (e: Exception) {
                log.error("Failed to seed database: ${e.message}", e)
            }
        } else {
            log.info("Running in production mode - skipping database seeding")
        }
    } catch (e: Exception) {
        log.warn("Could not determine development mode, assuming production: ${e.message}")
    }

    // Configure other plugins
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
}