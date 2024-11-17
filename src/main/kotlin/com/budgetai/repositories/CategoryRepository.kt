package com.budgetai.repositories

import com.budgetai.models.Categories
import com.budgetai.models.CategoryDTO
import com.budgetai.models.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.Entities
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Categories)
        }
    }

    suspend fun create(category: CategoryDTO): Int = dbQuery {
        Categories.insert {
            it[name] = category.name
            it[type] = category.type
            it[description] = category.description
        }[Categories.id].value
    }

    suspend fun findById(id: Int): CategoryDTO? = dbQuery {
        Categories.selectAll()
            .where { Categories.id eq id }
            .map(::toCategory)
            .singleOrNull()
    }

    suspend fun findByName(name: String): CategoryDTO? = dbQuery {
        Categories.selectAll()
            .where { Categories.name eq name }
            .map(::toCategory)
            .singleOrNull()
    }

    suspend fun findAll(): List<CategoryDTO> = dbQuery {
        Categories.selectAll()
            .map(::toCategory)
    }

    suspend fun findByType(type: CategoryType): List<CategoryDTO> = dbQuery {
        Categories.selectAll()
            .where { Categories.type eq type }
            .map(::toCategory)
    }

    suspend fun update(id: Int, category: CategoryDTO) = dbQuery {
        Categories.update({ Categories.id eq id }) {
            Entities.it[name] = category.name
            it[type] = category.type
            it[description] = category.description
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Categories.deleteWhere { Categories.id eq id }
    }

    private fun toCategory(row: ResultRow) = CategoryDTO(
        id = row[Categories.id].value,
        name = row[Categories.name],
        type = row[Categories.type],
        description = row[Categories.description],
        createdAt = row[Categories.createdAt].toLocalDateTime(TimeZone.UTC)
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}