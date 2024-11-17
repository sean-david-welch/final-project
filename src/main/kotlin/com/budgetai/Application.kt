package com.budgetai

import com.budgetai.lib.DataSeeder
import com.budgetai.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    System.setProperty("io.ktor.development", "true")
    System.setProperty("config.resource", "application-development.conf")
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseConfig.initialize()

    val isDevelopment = checkDevelopmentMode(this)
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

    // Configure other plugins
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
}

private fun checkDevelopmentMode(application: Application): Boolean {
    // Check multiple sources for development mode flag
    return try {
        // Check system property first
        System.getProperty("io.ktor.development")?.toBoolean()
        // Then check environment variable
            ?: System.getenv("KTOR_DEVELOPMENT")?.toBoolean()
            // Then check application config
            ?: application.environment.config.property("ktor.development").getString().toBoolean()
            // Default to false if none are set
            ?: false
    } catch (e: Exception) {
        application.log.warn("Could not determine development mode from config, assuming false: ${e.message}")
        false
    }
}