package com.budgetai.repositories

import com.budgetai.models.Categories
import com.budgetai.models.CategoryDTO
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CategoryRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(Categories)
        }
    }

    // Helper Methods
    // Maps database row to com.budgetai.models.CategoryDTO
    private fun toCategory(row: ResultRow) = CategoryDTO(
        id = row[Categories.id].value,
        name = row[Categories.name],
        type = row[Categories.type].toString(),
        description = row[Categories.description],
    )

    // Executes a database query within a coroutine context
    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }

    // Read Methods
    // Retrieves a category by its ID
    suspend fun findById(id: Int): CategoryDTO? = dbQuery {
        Categories.selectAll().where { Categories.id eq id }.map(::toCategory).singleOrNull()
    }

    suspend fun findByUserId(userId: Int): List<CategoryDTO> = dbQuery {
        Categories.selectAll().where { Categories.userId eq userId }.map(::toCategory)
    }

    // Retrieves a category by its name
    suspend fun findByName(name: String): CategoryDTO? = dbQuery {
        Categories.selectAll().where { Categories.name eq name }.map(::toCategory).singleOrNull()
    }

    // Retrieves all categories
    suspend fun findAll(): List<CategoryDTO> = dbQuery {
        Categories.selectAll().map(::toCategory)
    }

    // Retrieves all categories of a specific type
    suspend fun findByType(type: String): List<CategoryDTO> = dbQuery {
        Categories.selectAll().where { Categories.type eq type }.map(::toCategory)
    }

    // Write Methods
    // Creates a new category and returns its ID
    suspend fun create(category: CategoryDTO): Int = dbQuery {
        Categories.insert {
            it[name] = category.name
            it[type] = category.type
            it[description] = category.description
        }[Categories.id].value
    }

    // bulk
    suspend fun bulkCreate(categories: List<CategoryDTO>) = dbQuery {
        Categories.batchInsert(categories) {
            this[Categories.name] = it.name
            this[Categories.type] = it.type
            this[Categories.description] = it.description
            this[Categories.userId] = it.userId
        }
    }

    // Updates an existing category
    suspend fun update(id: Int, category: CategoryDTO) = dbQuery {
        Categories.update({ Categories.id eq id }) { stmt ->
            stmt[name] = category.name
            stmt[type] = category.type
            stmt[description] = category.description
        }
    }

    // Deletes a category by its ID
    suspend fun delete(id: Int) = dbQuery {
        Categories.deleteWhere { Categories.id eq id }
    }
}