package com.budgetai.plugins

import com.budgetai.database.FlywayMigrations
import org.jetbrains.exposed.sql.Database
import java.io.File

fun configureDatabases(): Database {
    val projectDir = File("src/main/kotlin/com/budgetai/database")
    val dbFile = projectDir.resolve("database.db")
    val jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
    FlywayMigrations.migrate(jdbcUrl)

    return Database.connect(
        url = "jdbc:sqlite:${dbFile.absolutePath}?journal_mode=WAL&foreign_keys=ON",
        driver = "org.sqlite.JDBC"
    )
}
