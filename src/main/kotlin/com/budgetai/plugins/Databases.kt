package com.budgetai.plugins

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

object DatabaseConfig {
    private var database: Database? = null
    private const val MIGRATIONS_LOCATION = "classpath:migrations"
    private val logger = LoggerFactory.getLogger("Database")

    data class DatabaseSettings(
        val journalMode: String = "WAL", val foreignKeys: Boolean = true, val migrationLocation: String = MIGRATIONS_LOCATION
    )

    fun initialize(settings: DatabaseSettings = DatabaseSettings()): Database {
        if (database == null) {
            logger.info("Starting database initialization...")

            val dbFile = resolveDbFile()
            logger.info("Database file resolved to: ${dbFile.absolutePath}")

            val baseJdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
            logger.info("Using JDBC URL: $baseJdbcUrl")

            try {
                logger.info("Starting database migration process...")
                migrateDatabase(baseJdbcUrl, settings.migrationLocation)
                logger.info("Database migration completed successfully")

                logger.info("Establishing database connection...")
                database = connectToDatabase(dbFile, settings)
                logger.info("Database connection established successfully")

                // Verify database file existence and accessibility
                if (dbFile.exists()) {
                    logger.info("Database file verification successful - Size: ${dbFile.length()} bytes")
                } else {
                    logger.error("Database file not found after initialization at: ${dbFile.absolutePath}")
                    throw IllegalStateException("Database file not created successfully")
                }
            } catch (e: Exception) {
                logger.error("Failed to initialize database", e)
                throw e
            }
        } else {
            logger.info("Using existing database connection")
        }
        return database!!
    }

    fun getDatabase(): Database {
        return database ?: throw IllegalStateException("Database has not been initialized").also {
            logger.error("Attempted to access database before initialization")
        }
    }

    private fun resolveDbFile(): File {
        val dbDirectory = File("/data")
        if (!dbDirectory.exists()) {
            try {
                dbDirectory.mkdirs()
                // Verify write permissions explicitly
                if (!dbDirectory.canWrite()) {
                    logger.error("No write permissions for database directory: ${dbDirectory.absolutePath}")
                    throw IOException("Cannot write to database directory")
                }
            } catch (e: SecurityException) {
                logger.error("Failed to create database directory due to permissions", e)
                throw e
            }
        }
        return dbDirectory.resolve("database.db")
    }

    private fun migrateDatabase(jdbcUrl: String, migrationLocation: String) {
        try {
            logger.info("Configuring Flyway with migration location: $migrationLocation")
            val flyway = Flyway.configure().dataSource(jdbcUrl, "", "").locations(migrationLocation).mixed(true).baselineOnMigrate(true)
                .load()

            val migrationResult = flyway.migrate()
            logger.info("Migration completed - ${migrationResult.migrationsExecuted} migrations executed")
        } catch (e: Exception) {
            logger.error("Database migration failed", e)
            throw e
        }
    }

    private fun connectToDatabase(dbFile: File, settings: DatabaseSettings): Database {
        val params = buildList {
            add("journal_mode=${settings.journalMode}")
            if (settings.foreignKeys) add("foreign_keys=ON")
        }.joinToString("&")

        val jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}?$params"
        logger.info("Connecting to database with parameters: $params")

        return try {
            Database.connect(
                url = jdbcUrl, driver = "org.sqlite.JDBC"
            ).also {
                logger.info("Database connection established successfully")
            }
        } catch (e: Exception) {
            logger.error("Failed to connect to database", e)
            throw e
        }
    }
}