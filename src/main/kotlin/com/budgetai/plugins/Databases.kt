package com.budgetai.plugins

import org.jetbrains.exposed.sql.Database
import java.io.File

fun configureDatabases() {
    val projectDir = File("src/main/kotlin/com/budgetai")
    val dbFile = projectDir.resolve("database.db")

    Database.connect(
        url = "jdbc:sqlite:${dbFile.absolutePath}?journal_mode=WAL&foreign_keys=ON",
        driver = "org.sqlite.JDBC"
    )
}
