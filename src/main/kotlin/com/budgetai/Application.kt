package com.budgetai

import com.budgetai.lib.DataSeeder
import com.budgetai.plugins.*
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main() {
    System.setProperty("io.ktor.development", "true")
    System.setProperty("config.resource", "development.conf")
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}

fun Application.module() {
    val config = HoconApplicationConfig(ConfigFactory.load())

    val database = DatabaseConfig.initialize()

    val isDevelopment = checkDevelopmentMode(this)
    if (isDevelopment) {
        log.info("Running in development mode - seeding database...")
        try {
            runBlocking {
                DataSeeder(database).seed()
            }
            log.info("Database seeding completed successfully")
        } catch (e: Exception) {
            log.error("Failed to seed database: ${e.message}", e)
        }
    } else {
        log.info("Running in production mode - skipping database seeding")
    }

    configureSecurity(config)
    configureHTTP()
    configureSerialization()
    configureRouting(config, database)
}

private fun checkDevelopmentMode(application: Application): Boolean {
    return try {
        System.getProperty("io.ktor.development")?.toBoolean() ?: System.getenv("KTOR_DEVELOPMENT")?.toBoolean()
        ?: application.environment.config.property("ktor.development").getString().toBoolean()
    } catch (e: Exception) {
        application.log.warn("Could not determine development mode from config, assuming false: ${e.message}")
        false
    }
}