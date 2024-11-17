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

    val shouldSeedData = environment.config.propertyOrNull("ktor.database.seed")?.getString()?.toBoolean() ?: false
    if (shouldSeedData) {
        log.info("Seeding database...")
        transaction {
            DataSeeder().seed(this)
        }
        log.info("Database seeding completed")
    }

    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting()
}
