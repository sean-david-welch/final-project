package com.budgetai.repositories

import com.budgetai.models.SavingsGoalDTO
import com.budgetai.models.SavingsGoals
import com.budgetai.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class SavingsGoalRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(SavingsGoals)
        }
    }

    // Helper Methods
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    private fun String.toLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    // Maps database row to SavingsGoalDTO
    private fun toSavingsGoal(row: ResultRow) = SavingsGoalDTO(
        id = row[SavingsGoals.id].value,
        userId = row[SavingsGoals.userId].value,
        name = row[SavingsGoals.name],
        description = row[SavingsGoals.description],
        targetAmount = row[SavingsGoals.targetAmount].toDouble(),
        currentAmount = row[SavingsGoals.currentAmount].toDouble(),
        targetDate = row[SavingsGoals.targetDate]?.toString(),
        createdAt = row[SavingsGoals.createdAt].toString()
    )

    // Read Methods
    // Retrieves a savings goal by its ID
    suspend fun findById(id: Int): SavingsGoalDTO? = dbQuery {
        SavingsGoals.selectAll()
            .where { SavingsGoals.id eq id }
            .map(::toSavingsGoal)
            .singleOrNull()
    }

    // Retrieves all savings goals for a given user ID
    suspend fun findByUserId(userId: Int): List<SavingsGoalDTO> = dbQuery {
        SavingsGoals.selectAll()
            .where { SavingsGoals.userId eq userId }
            .map(::toSavingsGoal)
    }

    // Retrieves active savings goals (not yet reached target amount)
    suspend fun findActiveByUserId(userId: Int): List<SavingsGoalDTO> = dbQuery {
        SavingsGoals.selectAll()
            .where {
                (SavingsGoals.userId eq userId) and
                (SavingsGoals.currentAmount less SavingsGoals.targetAmount)
            }
            .map(::toSavingsGoal)
    }

    // Retrieves completed savings goals
    suspend fun findCompletedByUserId(userId: Int): List<SavingsGoalDTO> = dbQuery {
        SavingsGoals.selectAll()
            .where {
                (SavingsGoals.userId eq userId) and
                (SavingsGoals.currentAmount greaterEq SavingsGoals.targetAmount)
            }
            .map(::toSavingsGoal)
    }

    // Retrieves upcoming goals (target date in the future)
    suspend fun findUpcomingByUserId(userId: Int): List<SavingsGoalDTO> = dbQuery {
        val today = LocalDate.parse(LocalDate.now().toString())
        SavingsGoals.selectAll()
            .where {
                (SavingsGoals.userId eq userId) and
                (SavingsGoals.targetDate greater today)
            }
            .map(::toSavingsGoal)
    }

    // Calculate progress percentage for a goal
    suspend fun calculateProgress(id: Int): Double = dbQuery {
        SavingsGoals.select(SavingsGoals.currentAmount, SavingsGoals.targetAmount)
            .where { SavingsGoals.id eq id }
            .map {
                val current = it[SavingsGoals.currentAmount].toDouble()
                val target = it[SavingsGoals.targetAmount].toDouble()
                if (target > 0) (current / target) * 100 else 0.0
            }
            .singleOrNull() ?: 0.0
    }

    // Get total savings across all goals for a user
    suspend fun getTotalSavings(userId: Int): Double = dbQuery {
        SavingsGoals.select(SavingsGoals.currentAmount.sum())
            .where { SavingsGoals.userId eq userId }
            .map { it[SavingsGoals.currentAmount.sum()]?.toDouble() ?: 0.0 }
            .single()
    }

    // Write Methods
    // Creates a new savings goal and returns its ID
    suspend fun create(goal: SavingsGoalDTO): Int = dbQuery {
        SavingsGoals.insertAndGetId { row ->
            row[userId] = EntityID(goal.userId, Users)
            row[name] = goal.name
            row[description] = goal.description
            row[targetAmount] = BigDecimal(goal.targetAmount)
            row[currentAmount] = BigDecimal(goal.currentAmount)
            row[targetDate] = goal.targetDate?.toLocalDate()
        }.value
    }

    // Updates an existing savings goal
    suspend fun update(id: Int, goal: SavingsGoalDTO) = dbQuery {
        SavingsGoals.update({ SavingsGoals.id eq id }) { stmt ->
            stmt[userId] = EntityID(goal.userId, Users)
            stmt[name] = goal.name
            stmt[description] = goal.description
            stmt[targetAmount] = BigDecimal(goal.targetAmount)
            stmt[currentAmount] = BigDecimal(goal.currentAmount)
            stmt[targetDate] = goal.targetDate?.toLocalDate()
        }
    }

    // Updates just the current amount of a savings goal
    suspend fun updateCurrentAmount(id: Int, amount: Double) = dbQuery {
        SavingsGoals.update({ SavingsGoals.id eq id }) { stmt ->
            stmt[currentAmount] = BigDecimal(amount)
        }
    }

    // Add to current amount
    suspend fun addToCurrentAmount(id: Int, amount: Double) = dbQuery {
        SavingsGoals.update({ SavingsGoals.id eq id }) { stmt ->
            stmt[currentAmount] = currentAmount + BigDecimal(amount)
        }
    }

    // Subtract from current amount
    suspend fun subtractFromCurrentAmount(id: Int, amount: Double) = dbQuery {
        SavingsGoals.update({ SavingsGoals.id eq id }) { stmt ->
            stmt[currentAmount] = currentAmount - BigDecimal(amount)
        }
    }

    // Deletes a savings goal by its ID
    suspend fun delete(id: Int) = dbQuery {
        SavingsGoals.deleteWhere { SavingsGoals.id eq id }
    }

    // Deletes all savings goals for a given user ID
    suspend fun deleteByUserId(userId: Int) = dbQuery {
        SavingsGoals.deleteWhere { SavingsGoals.userId eq userId }
    }

    // Analysis Methods
    // Check if goal is on track based on target date and current progress
    suspend fun isGoalOnTrack(id: Int): Boolean = dbQuery {
        val goal = findById(id) ?: return@dbQuery false

        val targetDate = goal.targetDate?.toLocalDate() ?: return@dbQuery false
        val today = LocalDate.parse(LocalDate.toString())

        val totalDays = today.daysUntil(targetDate)
        if (totalDays <= 0) return@dbQuery false

        val progress = goal.currentAmount / goal.targetAmount
        val timeProgress = (totalDays.toDouble() /
            today.daysUntil(goal.targetDate.toLocalDate()!!).toDouble())

        progress >= timeProgress
    }

    // Get remaining amount needed to reach goal
    suspend fun getRemainingAmount(id: Int): Double = dbQuery {
        SavingsGoals.select(SavingsGoals.targetAmount, SavingsGoals.currentAmount)
            .where { SavingsGoals.id eq id }
            .map {
                val target = it[SavingsGoals.targetAmount].toDouble()
                val current = it[SavingsGoals.currentAmount].toDouble()
                maxOf(0.0, target - current)
            }
            .singleOrNull() ?: 0.0
    }

    // Calculate required daily savings to reach goal by target date
    suspend fun getRequiredDailySavings(id: Int): Double = dbQuery {
        val goal = findById(id) ?: return@dbQuery 0.0
        val targetDate = goal.targetDate?.toLocalDate() ?: return@dbQuery 0.0
        val today = LocalDate.parse(LocalDate.toString())

        val remainingDays = today.daysUntil(targetDate)
        if (remainingDays <= 0) return@dbQuery 0.0

        val remainingAmount = getRemainingAmount(id)
        remainingAmount / remainingDays.toDouble()
    }
}