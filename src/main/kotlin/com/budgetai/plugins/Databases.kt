package com.budgetai.plugins

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.io.File

object DatabaseConfig {
    private var database: Database? = null
    private const val MIGRATIONS_LOCATION = "classpath:migrations"

    data class DatabaseSettings(
        val journalMode: String = "WAL", val foreignKeys: Boolean = true, val migrationLocation: String = MIGRATIONS_LOCATION
    )

    fun initialize(settings: DatabaseSettings = DatabaseSettings()): Database {
        if (database == null) {
            val dbFile = resolveDbFile()
            val baseJdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"

            migrateDatabase(baseJdbcUrl, settings.migrationLocation)
            database = connectToDatabase(dbFile, settings)
        }
        return database!!
    }

    fun getDatabase(): Database = database ?: throw IllegalStateException("Database has not been initialized")

    private fun resolveDbFile(): File {
        val projectDir = File("src/main/kotlin/com/budgetai/database")
        return projectDir.resolve("database.db").also {
            it.parentFile.mkdirs()
        }
    }

    private fun migrateDatabase(jdbcUrl: String, migrationLocation: String) {
        Flyway.configure().dataSource(jdbcUrl, "", "").locations(migrationLocation).mixed(true).baselineOnMigrate(true).load().migrate()
    }

    private fun connectToDatabase(dbFile: File, settings: DatabaseSettings): Database {
        val params = buildList {
            add("journal_mode=${settings.journalMode}")
            if (settings.foreignKeys) add("foreign_keys=ON")
        }.joinToString("&")

        val jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}?$params"

        return Database.connect(
            url = jdbcUrl, driver = "org.sqlite.JDBC"
        )
    }
}