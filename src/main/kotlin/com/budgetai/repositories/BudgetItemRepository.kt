package com.budgetai.repositories

import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.BudgetItems
import com.budgetai.models.Budgets
import com.budgetai.models.Categories
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class BudgetItemRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(BudgetItems)
        }
    }

    // Helper Methods
    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }

    // Maps database row to BudgetItemDTO
    private fun toBudgetItem(row: ResultRow) = BudgetItemDTO(
        id = row[BudgetItems.id].value, budgetId = row[BudgetItems.budgetId].value, categoryId = row[BudgetItems.categoryId].value,
        name = row[BudgetItems.name], amount = row[BudgetItems.amount].toDouble(), createdAt = row[BudgetItems.createdAt].toString()
    )

    // Read Methods
    // Retrieves a budget item by its ID
    suspend fun findById(id: Int): BudgetItemDTO? = dbQuery {
        BudgetItems.selectAll().where { BudgetItems.id eq id }.map(::toBudgetItem).singleOrNull()
    }

    // Retrieves all budget items for a given budget ID
    suspend fun findByBudgetId(budgetId: Int): List<BudgetItemDTO> = dbQuery {
        BudgetItems.selectAll().where { BudgetItems.budgetId eq budgetId }.map(::toBudgetItem)
    }

    // Retrieves all budget items for a budget with a user id
    suspend fun findByUserId(userId: Int): List<BudgetItemDTO> = dbQuery {
        BudgetItems.join(Budgets, JoinType.INNER, BudgetItems.budgetId, Budgets.id).selectAll().where { Budgets.userId eq userId }
            .map(::toBudgetItem)
    }
    // Retrieves all budget items for a given category ID
    suspend fun findByCategoryId(categoryId: Int): List<BudgetItemDTO> = dbQuery {
        BudgetItems.selectAll().where { BudgetItems.categoryId eq categoryId }.map(::toBudgetItem)
    }

    // Get total amount of budget items for a budget
    suspend fun getTotalAmountByBudgetId(budgetId: Int): Double = dbQuery {
        BudgetItems.select(BudgetItems.amount.sum()).where { BudgetItems.budgetId eq budgetId }
            .map { it[BudgetItems.amount.sum()]?.toDouble() ?: 0.0 }.single()
    }

    // Get total amount of budget items by category within a budget
    suspend fun getTotalAmountByCategory(budgetId: Int, categoryId: Int): Double = dbQuery {
        BudgetItems.select(BudgetItems.amount.sum()).where { (BudgetItems.budgetId eq budgetId) and (BudgetItems.categoryId eq categoryId) }
            .map { it[BudgetItems.amount.sum()]?.toDouble() ?: 0.0 }.single()
    }

    // Write Methods
    // Creates a new budget item and returns its ID
    suspend fun create(budgetItem: BudgetItemDTO): Int = dbQuery {
        BudgetItems.insertAndGetId { row ->
            row[budgetId] = EntityID(budgetItem.budgetId, Budgets)
            row[categoryId] = EntityID(budgetItem.categoryId, Categories)
            row[name] = budgetItem.name
            row[amount] = BigDecimal(budgetItem.amount)
        }.value
    }

    // Updates an existing budget item
    suspend fun update(id: Int, budgetItem: BudgetItemDTO) = dbQuery {
        BudgetItems.update({ BudgetItems.id eq id }) { stmt ->
            stmt[budgetId] = EntityID(budgetItem.budgetId, Budgets)
            stmt[categoryId] = EntityID(budgetItem.categoryId, Categories)
            stmt[name] = budgetItem.name
            stmt[amount] = BigDecimal(budgetItem.amount)
        }
    }

    // Updates just the amount of a budget item
    suspend fun updateAmount(id: Int, amount: Double) = dbQuery {
        BudgetItems.update({ BudgetItems.id eq id }) { stmt ->
            stmt[BudgetItems.amount] = BigDecimal(amount)
        }
    }

    // Deletes a budget item by its ID
    suspend fun delete(id: Int) = dbQuery {
        BudgetItems.deleteWhere { BudgetItems.id eq id }
    }

    // Deletes all budget items for a given budget ID
    suspend fun deleteByBudgetId(budgetId: Int) = dbQuery {
        BudgetItems.deleteWhere { BudgetItems.budgetId eq budgetId }
    }

    // Deletes all budget items for a given category ID
    suspend fun deleteByCategoryId(categoryId: Int) = dbQuery {
        BudgetItems.deleteWhere { BudgetItems.categoryId eq categoryId }
    }

    // Batch Methods
    // Creates multiple budget items at once
    suspend fun createBatch(budgetItems: List<BudgetItemDTO>): List<Int> = dbQuery {
        BudgetItems.batchInsert(budgetItems) { item ->
            this[BudgetItems.budgetId] = EntityID(item.budgetId, Budgets)
            this[BudgetItems.categoryId] = EntityID(item.categoryId, Categories)
            this[BudgetItems.name] = item.name
            this[BudgetItems.amount] = BigDecimal(item.amount)
        }.map { it[BudgetItems.id].value }
    }
}