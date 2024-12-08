package com.budgetai.repositories

import com.budgetai.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class BudgetRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(Budgets)
        }
    }

    // Helper Methods
    // Converts string to LocalDate, returns null if invalid
    private fun String.toLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    // Executes a database query within a coroutine context
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    // Maps database row to BudgetDTO
    private fun toBudget(row: ResultRow) = BudgetDTO(
        id = row[Budgets.id].value,
        userId = row[Budgets.userId].value,
        name = row[Budgets.name],
        description = row[Budgets.description],
        startDate = row[Budgets.startDate]?.toString(),
        endDate = row[Budgets.endDate]?.toString(),
        totalIncome = row[Budgets.totalIncome].toDouble(),
        totalExpenses = row[Budgets.totalExpenses].toDouble(),
        createdAt = row[Budgets.createdAt].toString()
    )

    // Read Methods
    // Retrieves a budget by its ID
    suspend fun findAll(): List<BudgetDTO> = dbQuery { Budgets.selectAll().map { toBudget(it) } }

    suspend fun findById(id: Int): BudgetDTO? = dbQuery {
        Budgets.selectAll().where { Budgets.id eq id }.map(::toBudget).singleOrNull()
    }

    // Retrieves all budgets for a given user ID
    suspend fun findByUserId(userId: Int): List<BudgetDTO> = dbQuery {
        Budgets.selectAll().where { Budgets.userId eq userId }.map(::toBudget)
    }

    // join query
    suspend fun findByUserIdWithDetails(userId: Int): List<BudgetWithItemsDTO> = dbQuery {
        // First get all the budgets for the user
        (Budgets
            .leftJoin(BudgetItems)
            .leftJoin(Categories))
            .selectAll().where { Budgets.userId eq userId }
            .orderBy(Budgets.id)
            .groupBy(
                { it[Budgets.id] },
                { row ->
                    // Map the budget
                    BudgetWithItemsDTO(
                        id = row[Budgets.id].value,
                        userId = row[Budgets.userId].value,
                        name = row[Budgets.name],
                        description = row[Budgets.description],
                        startDate = row[Budgets.startDate]?.toString(),
                        endDate = row[Budgets.endDate]?.toString(),
                        totalIncome = row[Budgets.totalIncome].toDouble(),
                        totalExpenses = row[Budgets.totalExpenses].toDouble(),
                        createdAt = row[Budgets.createdAt].toString(),
                        items = emptyList() // Will be populated in the transform step
                    )
                },
                { budgetId, rows, budget ->
                    // Transform to include budget items
                    budget.copy(
                        items = rows.mapNotNull { row ->
                            row.getOrNull(BudgetItems.id)?.let { itemId ->
                                BudgetItemWithCategoryDTO(
                                    id = itemId.value,
                                    name = row[BudgetItems.name],
                                    amount = row[BudgetItems.amount].toDouble(),
                                    category = row.getOrNull(Categories.id)?.let { categoryId ->
                                        CategoryDTO(
                                            id = categoryId.value,
                                            name = row[Categories.name],
                                            type = row[Categories.type],
                                            description = row[Categories.description]
                                        )
                                    }
                                )
                            }
                        }.distinctBy { it.id }
                    )
                }
            ).values.toList()
    }

    // Retrieves budgets for a user within a specified date range
    suspend fun findByUserIdAndDateRange(
        userId: Int, startDate: String, endDate: String
    ): List<BudgetDTO> = dbQuery {
        val start = startDate.toLocalDate()
        val end = endDate.toLocalDate()

        if (start == null || end == null) {
            emptyList()
        } else {
            Budgets.selectAll().where {
                (Budgets.userId eq userId) and (Budgets.startDate lessEq end) and (Budgets.endDate greaterEq start)
            }.map(::toBudget)
        }
    }

    // Write Methods
    // Creates a new budget and returns its ID
    suspend fun create(budget: BudgetDTO): Int = dbQuery {
        Budgets.insertAndGetId { row ->
            row[userId] = EntityID(budget.userId, Users)
            row[name] = budget.name
            row[description] = budget.description
            row[startDate] = budget.startDate?.toLocalDate()
            row[endDate] = budget.endDate?.toLocalDate()
            row[totalIncome] = BigDecimal(budget.totalIncome)
            row[totalExpenses] = BigDecimal(budget.totalExpenses)
        }.value
    }

    // Updates all fields of an existing budget
    suspend fun update(id: Int, budget: BudgetDTO) = dbQuery {
        Budgets.update({ Budgets.id eq id }) { stmt ->
            stmt[userId] = EntityID(budget.userId, Users)
            stmt[name] = budget.name
            stmt[description] = budget.description
            stmt[startDate] = budget.startDate?.toLocalDate()
            stmt[endDate] = budget.endDate?.toLocalDate()
            stmt[totalIncome] = BigDecimal(budget.totalIncome)
            stmt[totalExpenses] = BigDecimal(budget.totalExpenses)
        }
    }

    // Updates only the total income and expenses of a budget
    suspend fun updateTotals(id: Int, totalIncome: Double, totalExpenses: Double) = dbQuery {
        Budgets.update({ Budgets.id eq id }) { stmt ->
            stmt[Budgets.totalIncome] = BigDecimal(totalIncome)
            stmt[Budgets.totalExpenses] = BigDecimal(totalExpenses)
        }
    }

    // Deletes a budget by its ID
    suspend fun delete(id: Int) = dbQuery {
        Budgets.deleteWhere { Budgets.id eq id }
    }

    // Deletes all budgets for a given user ID
    suspend fun deleteByUserId(userId: Int) = dbQuery {
        Budgets.deleteWhere { Budgets.userId eq userId }
    }
}