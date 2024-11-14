package com.budgetai.database

import org.flywaydb.core.Flyway

object FlywayMigrations {
    fun migrate(jdbcUrl: String) {
        Flyway.configure()
            .dataSource(jdbcUrl, "", "")
            .locations("database/migration")
            .load()
            .migrate()
    }
}
