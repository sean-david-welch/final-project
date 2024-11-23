package com.budgetai.services

import com.budgetai.models.BudgetItemDTO
import com.budgetai.repositories.BudgetItemRepository

class BudgetItemService(private val repository: BudgetItemRepository) {
    // Data Models
    data class BudgetItemCreationRequest(
        val budgetId: Int, val categoryId: Int, val name: String, val amount: Double
    )

    data class BudgetItemUpdateRequest(
        val name: String? = null, val categoryId: Int? = null, val amount: Double? = null
    )

    // Helper Methods
    private fun validateAmount(amount: Double) {
        require(amount >= 0) { "Amount cannot be negative" }
    }

    private suspend fun validateBudgetItemExists(id: Int): BudgetItemDTO {
        return repository.findById(id) ?: throw IllegalArgumentException("Budget item not found")
    }

    // Read Methods
    suspend fun getBudgetItem(id: Int): BudgetItemDTO? {
        return repository.findById(id)
    }

    suspend fun getBudgetItems(budgetId: Int): List<BudgetItemDTO> {
        return repository.findByBudgetId(budgetId)
    }

    suspend fun getCategoryItems(categoryId: Int): List<BudgetItemDTO> {
        return repository.findByCategoryId(categoryId)
    }

    suspend fun getBudgetTotalAmount(budgetId: Int): Double {
        return repository.getTotalAmountByBudgetId(budgetId)
    }

    suspend fun getCategoryTotalAmount(budgetId: Int, categoryId: Int): Double {
        return repository.getTotalAmountByCategory(budgetId, categoryId)
    }

    // Write Methods
    suspend fun createBudgetItem(request: BudgetItemCreationRequest): Int {
        validateAmount(request.amount)

        val budgetItemDTO = BudgetItemDTO(
            id = 0, // Will be set by the database
            budgetId = request.budgetId,
            categoryId = request.categoryId,
            name = request.name,
            amount = request.amount,
            createdAt = null // Will be set by the database
        )

        return repository.create(budgetItemDTO)
    }

    suspend fun createBulkBudgetItems(requests: List<BudgetItemCreationRequest>): List<Int> {
        requests.forEach { validateAmount(it.amount) }

        val budgetItems = requests.map { request ->
            BudgetItemDTO(
                id = 0,
                budgetId = request.budgetId,
                categoryId = request.categoryId,
                name = request.name,
                amount = request.amount,
                createdAt = null
            )
        }

        return repository.createBatch(budgetItems)
    }

    suspend fun updateBudgetItem(id: Int, request: BudgetItemUpdateRequest) {
        val existingItem = validateBudgetItemExists(id)

        request.amount?.let { validateAmount(it) }

        val updatedItem = existingItem.copy(
            name = request.name ?: existingItem.name,
            categoryId = request.categoryId ?: existingItem.categoryId,
            amount = request.amount ?: existingItem.amount
        )

        repository.update(id, updatedItem)
    }

    suspend fun updateBudgetItemAmount(id: Int, amount: Double) {
        validateBudgetItemExists(id)
        validateAmount(amount)
        repository.updateAmount(id, amount)
    }

    suspend fun deleteBudgetItem(id: Int) {
        validateBudgetItemExists(id)
        repository.delete(id)
    }

    suspend fun deleteBudgetItems(budgetId: Int) {
        repository.deleteByBudgetId(budgetId)
    }

    suspend fun deleteCategoryItems(categoryId: Int) {
        repository.deleteByCategoryId(categoryId)
    }
}