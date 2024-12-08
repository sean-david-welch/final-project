package com.budgetai

import com.budgetai.lib.DataSeeder
import com.budgetai.plugins.*
import com.budgetai.utils.TemplateContext
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
    try {
        runBlocking {
            DataSeeder(database).seed()
        }
        log.info("Database seeding completed successfully")
    } catch (e: Exception) {
        log.error("Failed to seed database: ${e.message}", e)
    }

    // custom plugins
    install(TemplateContext)
    // default plugin configurations
    configureSecurity(config)
    configureHTTP()
    configureSerialization()
    configureRouting(config, database)
}

