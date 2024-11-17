package com.budgetai.repositories

import com.budgetai.models.BudgetDTO
import com.budgetai.models.Budgets
import com.budgetai.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.ZoneOffset

class BudgetRepository(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Budgets)
        }
    }

    private fun String.toLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (e: Exception) {
            null
        }
    }

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

    suspend fun findById(id: Int): BudgetDTO? = dbQuery {
        Budgets.selectAll()
            .where { Budgets.id eq id }
            .map(::toBudget)
            .singleOrNull()
    }

    suspend fun findByUserId(userId: Int): List<BudgetDTO> = dbQuery {
        Budgets.selectAll()
            .where { Budgets.userId eq userId }
            .map(::toBudget)
    }

    suspend fun findByUserIdAndDateRange(
        userId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<BudgetDTO> = dbQuery {
        Budgets.selectAll()
            .where {
                (Budgets.userId eq userId) and
                (Budgets.startDate lessEq java.sql.Date.valueOf(endDate)) and
                (Budgets.endDate greaterEq java.sql.Date.valueOf(startDate))
            }
            .map(::toBudget)
    }

    suspend fun update(id: Int, budget: BudgetDTO) = dbQuery {
        Budgets.update({ Budgets.id eq id }) { stmt ->
            stmt[userId] = budget.userId
            stmt[name] = budget.name
            stmt[description] = budget.description
            stmt[startDate] = budget.startDate?.let { date -> java.sql.Date.valueOf(date) }
            stmt[endDate] = budget.endDate?.let { date -> java.sql.Date.valueOf(date) }
            stmt[totalIncome] = budget.totalIncome
            stmt[totalExpenses] = budget.totalExpenses
        }
    }

    suspend fun updateTotals(id: Int, totalIncome: BigDecimal, totalExpenses: BigDecimal) = dbQuery {
        Budgets.update({ Budgets.id eq id }) { stmt ->
            stmt[Budgets.totalIncome] = totalIncome
            stmt[Budgets.totalExpenses] = totalExpenses
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Budgets.deleteWhere { Budgets.id eq id }
    }

    suspend fun deleteByUserId(userId: Int) = dbQuery {
        Budgets.deleteWhere { Budgets.userId eq userId }
    }

    private fun toBudget(row: ResultRow) = BudgetDTO(
        id = row[Budgets.id].value,
        userId = row[Budgets.userId].value,
        name = row[Budgets.name],
        description = row[Budgets.description],
        startDate = row[Budgets.startDate]?.toLocalDate(),
        endDate = row[Budgets.endDate]?.toLocalDate(),
        totalIncome = row[Budgets.totalIncome],
        totalExpenses = row[Budgets.totalExpenses],
        createdAt = row[Budgets.createdAt].toLocalDateTime(TimeZone.UTC)
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}