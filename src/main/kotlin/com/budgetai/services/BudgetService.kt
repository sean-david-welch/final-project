package com.budgetai.services

import com.budgetai.models.BudgetCreationRequest
import com.budgetai.models.BudgetDTO
import com.budgetai.models.BudgetWithItemsDTO
import com.budgetai.repositories.BudgetRepository
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

class BudgetService(private val repository: BudgetRepository) {

    // Helper Methods
    // Validates that start date is before or equal to end date
    private fun validateDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        if (startDate != null && endDate != null) {
            require(startDate <= endDate) { "Start date must be before or equal to end date" }
        }
    }

    // Validates that a budget exists and returns it or throws exception
    private suspend fun validateBudgetExists(id: Int): BudgetDTO {
        return repository.findById(id) ?: throw IllegalArgumentException("Budget not found")
    }

    // Read Methods
    // Retrieves a single budget by ID
    suspend fun getBudgets(): List<BudgetDTO> {
        return repository.findAll()
    }

    suspend fun getBudget(id: Int): BudgetDTO? {
        return repository.findById(id)
    }

    // Retrieves all budgets for a user
    suspend fun getUserBudgets(userId: Int): List<BudgetDTO> {
        return repository.findByUserId(userId)
    }

    suspend fun getUserBudgetsWithItems(userId: Int): List<BudgetWithItemsDTO> {
        return repository.findByUserIdWithDetails(userId)
    }

    // Retrieves budgets for a user within a date range
    suspend fun getUserBudgetsInDateRange(
        userId: Int, startDate: LocalDate, endDate: LocalDate
    ): List<BudgetDTO> {
        validateDateRange(startDate, endDate)
        return repository.findByUserIdAndDateRange(userId, startDate.toString(), endDate.toString())
    }

    // Write Methods
    // Creates a new budget and returns its ID
    suspend fun createBudget(request: BudgetCreationRequest): Int {
        validateDateRange(request.startDate, request.endDate)

        val budgetDTO = BudgetDTO(
            userId = request.userId,
            name = request.name,
            description = request.description,
            totalIncome = request.totalIncome,
            totalExpenses = request.totalExpenses,
            startDate = request.startDate?.toString(),
            endDate = request.endDate?.toString()
        )

        return repository.create(budgetDTO)
    }

    // Updates an existing budget's details
    suspend fun updateBudget(id: Int, budget: BudgetDTO) {
        val existingBudget = validateBudgetExists(id)

        require(budget.userId == existingBudget.userId) {
            "Cannot change budget ownership"
        }

        budget.startDate?.let { start ->
            budget.endDate?.let { end ->
                require(start <= end) { "Start date must be before or equal to end date" }
            }
        }

        repository.update(id, budget)
    }

    // Updates only the total income and expenses of a budget
    suspend fun updateBudgetTotals(
        id: Int, totalIncome: BigDecimal, totalExpenses: BigDecimal
    ) {
        validateBudgetExists(id)

        require(totalIncome >= BigDecimal.ZERO) { "Total income cannot be negative" }
        require(totalExpenses >= BigDecimal.ZERO) { "Total expenses cannot be negative" }

        repository.updateTotals(id, totalIncome.toDouble(), totalExpenses.toDouble())
    }

    // Deletes a single budget
    suspend fun deleteBudget(id: Int) {
        validateBudgetExists(id)
        repository.delete(id)
    }

    // Deletes all budgets for a user
    suspend fun deleteUserBudgets(userId: Int) {
        repository.deleteByUserId(userId)
    }
}