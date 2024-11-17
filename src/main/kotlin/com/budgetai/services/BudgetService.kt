package com.budgetai.services

import com.budgetai.models.BudgetDTO
import com.budgetai.repositories.BudgetRepository
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

class BudgetService(private val repository: BudgetRepository) {

    data class BudgetCreationRequest(
        val userId: Int,
        val name: String,
        val description: String? = null,
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null
    )

    suspend fun createBudget(request: BudgetCreationRequest): Int {
        // Validate dates if provided
        if (request.startDate != null && request.endDate != null) {
            require(request.startDate <= request.endDate) {
                "Start date must be before or equal to end date"
            }
        }

        val budgetDTO = BudgetDTO(
            userId = request.userId,
            name = request.name,
            description = request.description,
            startDate = request.startDate.toString(),
            endDate = request.endDate.toString()
        )

        return repository.create(budgetDTO)
    }

    suspend fun getBudget(id: Int): BudgetDTO? {
        return repository.findById(id)
    }

    suspend fun getUserBudgets(userId: Int): List<BudgetDTO> {
        return repository.findByUserId(userId)
    }

    suspend fun getUserBudgetsInDateRange(
        userId: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<BudgetDTO> {
        require(startDate <= endDate) { "Start date must be before or equal to end date" }
        return repository.findByUserIdAndDateRange(userId, startDate.toString(), endDate.toString())
    }

    suspend fun updateBudget(id: Int, budget: BudgetDTO) {
        // Validate if budget exists
        val existingBudget = repository.findById(id)
            ?: throw IllegalArgumentException("Budget not found")

        // Validate dates if provided
        if (budget.startDate != null && budget.endDate != null) {
            require(budget.startDate <= budget.endDate) {
                "Start date must be before or equal to end date"
            }
        }

        // Ensure we're not changing the user ID
        require(budget.userId == existingBudget.userId) {
            "Cannot change budget ownership"
        }

        repository.update(id, budget)
    }

    suspend fun updateBudgetTotals(
        id: Int,
        totalIncome: BigDecimal,
        totalExpenses: BigDecimal
    ) {
        // Validate if budget exists
        repository.findById(id) ?: throw IllegalArgumentException("Budget not found")

        require(totalIncome >= BigDecimal.ZERO) { "Total income cannot be negative" }
        require(totalExpenses >= BigDecimal.ZERO) { "Total expenses cannot be negative" }

        repository.updateTotals(id, totalIncome.toDouble(), totalExpenses.toDouble())
    }

    suspend fun deleteBudget(id: Int) {
        repository.findById(id) ?: throw IllegalArgumentException("Budget not found")
        repository.delete(id)
    }

    suspend fun deleteUserBudgets(userId: Int) {
        repository.deleteByUserId(userId)
    }
}