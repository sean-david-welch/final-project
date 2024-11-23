package com.budgetai.services

import com.budgetai.models.SavingsGoalDTO
import com.budgetai.repositories.SavingsGoalRepository
import kotlinx.datetime.*
import java.math.BigDecimal

class SavingsGoalService(private val repository: SavingsGoalRepository) {
    // Data Models
    data class SavingsGoalCreationRequest(
        val userId: Int,
        val name: String,
        val description: String? = null,
        val targetAmount: Double,
        val initialAmount: Double = 0.0,
        val targetDate: String? = null
    )

    data class SavingsGoalUpdateRequest(
        val name: String? = null,
        val description: String? = null,
        val targetAmount: Double? = null,
        val targetDate: String? = null
    )

    data class GoalProgress(
        val currentAmount: Double,
        val targetAmount: Double,
        val percentageComplete: Double,
        val remainingAmount: Double,
        val isOnTrack: Boolean,
        val requiredDailySavings: Double
    )

    // Helper Methods
    private fun validateAmounts(targetAmount: Double, currentAmount: Double = 0.0) {
        require(targetAmount > 0) { "Target amount must be positive" }
        require(currentAmount >= 0) { "Current amount cannot be negative" }
        require(currentAmount <= targetAmount) { "Current amount cannot exceed target amount" }
    }

    private fun validateDate(targetDate: String?) {
        targetDate?.let {
            val parsedDate = LocalDate.parse(it)
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            require(parsedDate > today) { "Target date must be in the future" }
        }
    }

    private suspend fun validateGoalExists(id: Int): SavingsGoalDTO {
        return repository.findById(id) ?: throw IllegalArgumentException("Savings goal not found")
    }

    // Read Methods
    suspend fun getSavingsGoal(id: Int): SavingsGoalDTO? {
        return repository.findById(id)
    }

    suspend fun getUserSavingsGoals(userId: Int): List<SavingsGoalDTO> {
        return repository.findByUserId(userId)
    }

    suspend fun getActiveSavingsGoals(userId: Int): List<SavingsGoalDTO> {
        return repository.findActiveByUserId(userId)
    }

    suspend fun getCompletedSavingsGoals(userId: Int): List<SavingsGoalDTO> {
        return repository.findCompletedByUserId(userId)
    }

    suspend fun getUpcomingSavingsGoals(userId: Int): List<SavingsGoalDTO> {
        return repository.findUpcomingByUserId(userId)
    }

    suspend fun getTotalUserSavings(userId: Int): Double {
        return repository.getTotalSavings(userId)
    }

    // Analysis Methods
    suspend fun getGoalProgress(id: Int): GoalProgress {
        val goal = validateGoalExists(id)
        val percentageComplete = repository.calculateProgress(id)
        val remainingAmount = repository.getRemainingAmount(id)
        val isOnTrack = repository.isGoalOnTrack(id)
        val requiredDailySavings = repository.getRequiredDailySavings(id)

        return GoalProgress(
            currentAmount = goal.currentAmount,
            targetAmount = goal.targetAmount,
            percentageComplete = percentageComplete,
            remainingAmount = remainingAmount,
            isOnTrack = isOnTrack,
            requiredDailySavings = requiredDailySavings
        )
    }

    // Write Methods
    suspend fun createSavingsGoal(request: SavingsGoalCreationRequest): Int {
        validateAmounts(request.targetAmount, request.initialAmount)
        validateDate(request.targetDate)

        val goalDTO = SavingsGoalDTO(
            id = 0, // Will be set by database
            userId = request.userId,
            name = request.name,
            description = request.description,
            targetAmount = request.targetAmount,
            currentAmount = request.initialAmount,
            targetDate = request.targetDate,
            createdAt = null // Will be set by database
        )

        return repository.create(goalDTO)
    }

    suspend fun updateSavingsGoal(id: Int, request: SavingsGoalUpdateRequest) {
        val existingGoal = validateGoalExists(id)

        // Validate new target amount if provided
        request.targetAmount?.let {
            validateAmounts(it, existingGoal.currentAmount)
        }
        validateDate(request.targetDate)

        val updatedGoal = existingGoal.copy(
            name = request.name ?: existingGoal.name,
            description = request.description ?: existingGoal.description,
            targetAmount = request.targetAmount ?: existingGoal.targetAmount,
            targetDate = request.targetDate ?: existingGoal.targetDate
        )

        repository.update(id, updatedGoal)
    }

    suspend fun addContribution(id: Int, amount: Double) {
        require(amount > 0) { "Contribution amount must be positive" }
        val goal = validateGoalExists(id)

        val newTotal = goal.currentAmount + amount
        require(newTotal <= goal.targetAmount) { "Contribution would exceed target amount" }

        repository.addToCurrentAmount(id, amount)
    }

    suspend fun withdrawAmount(id: Int, amount: Double) {
        require(amount > 0) { "Withdrawal amount must be positive" }
        val goal = validateGoalExists(id)

        require(amount <= goal.currentAmount) { "Cannot withdraw more than current amount" }

        repository.subtractFromCurrentAmount(id, amount)
    }

    suspend fun updateCurrentAmount(id: Int, amount: Double) {
        val goal = validateGoalExists(id)
        validateAmounts(goal.targetAmount, amount)
        repository.updateCurrentAmount(id, amount)
    }

    suspend fun deleteSavingsGoal(id: Int) {
        validateGoalExists(id)
        repository.delete(id)
    }

    suspend fun deleteUserSavingsGoals(userId: Int) {
        repository.deleteByUserId(userId)
    }
}