package com.budgetai.services

import com.budgetai.models.AiInsightDTO
import com.budgetai.models.InsightType
import com.budgetai.models.Sentiment
import com.budgetai.repositories.AiInsightRepository
import kotlinx.datetime.*
import kotlinx.serialization.json.JsonElement

class AiInsightService(private val repository: AiInsightRepository) {
    // Data Models
    data class InsightCreationRequest(
        val userId: Int,
        val budgetId: Int,
        val budgetItemId: Int? = null,
        val prompt: String,
        val response: String,
        val type: InsightType,
        val sentiment: Sentiment? = null,
        val metadata: JsonElement? = null
    )

    data class InsightUpdateRequest(
        val prompt: String? = null,
        val response: String? = null,
        val type: InsightType? = null,
        val sentiment: Sentiment? = null,
        val metadata: JsonElement? = null
    )

    data class InsightAnalytics(
        val typeDistribution: Map<InsightType, Int>,
        val sentimentDistribution: Map<Sentiment, Int>,
        val totalInsights: Int,
        val recentInsights: List<AiInsightDTO>
    )

    // Helper Methods
    private fun validatePrompt(prompt: String) {
        require(prompt.isNotBlank()) { "Prompt cannot be empty" }
        require(prompt.length <= 1000) { "Prompt exceeds maximum length of 1000 characters" }
    }

    private fun validateResponse(response: String) {
        require(response.isNotBlank()) { "Response cannot be empty" }
        require(response.length <= 5000) { "Response exceeds maximum length of 5000 characters" }
    }

    private suspend fun validateInsightExists(id: Int): AiInsightDTO {
        return repository.findById(id) ?: throw IllegalArgumentException("Insight not found")
    }

    private fun validateDateRange(startDate: LocalDateTime, endDate: LocalDateTime) {
        require(startDate <= endDate) { "Start date must be before or equal to end date" }
    }

    // Read Methods
    suspend fun getInsight(id: Int): AiInsightDTO? {
        return repository.findById(id)
    }

    suspend fun getUserInsights(userId: Int): List<AiInsightDTO> {
        return repository.findByUserId(userId)
    }

    suspend fun getBudgetInsights(budgetId: Int): List<AiInsightDTO> {
        return repository.findByBudgetId(budgetId)
    }

    suspend fun getBudgetItemInsights(budgetItemId: Int): List<AiInsightDTO> {
        return repository.findByBudgetItemId(budgetItemId)
    }

    suspend fun getInsightsByType(type: InsightType): List<AiInsightDTO> {
        return repository.findByType(type)
    }

    suspend fun getInsightsBySentiment(sentiment: Sentiment): List<AiInsightDTO> {
        return repository.findBySentiment(sentiment)
    }

    suspend fun getInsightsInDateRange(
        userId: Int,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<AiInsightDTO> {
        validateDateRange(startDate, endDate)
        return repository.findByUserIdAndDateRange(userId, startDate, endDate)
    }

    // Analysis Methods
    suspend fun getUserInsightAnalytics(userId: Int): InsightAnalytics {
        val typeDistribution = repository.getInsightTypeDistribution(userId)
        val sentimentDistribution = repository.getSentimentDistribution(userId)
        val recentInsights = repository.getRecentInsights(userId, limit = 5)

        return InsightAnalytics(
            typeDistribution = typeDistribution,
            sentimentDistribution = sentimentDistribution,
            totalInsights = typeDistribution.values.sum(),
            recentInsights = recentInsights
        )
    }

    suspend fun getRecentInsightsPaginated(
        userId: Int,
        page: Int,
        pageSize: Int = 10
    ): List<AiInsightDTO> {
        require(page >= 0) { "Page number must be non-negative" }
        require(pageSize in 1..50) { "Page size must be between 1 and 50" }

        return repository.getRecentInsights(
            userId = userId,
            limit = pageSize,
            offset = page * pageSize
        )
    }

    // Write Methods
    suspend fun createInsight(request: InsightCreationRequest): Int {
        validatePrompt(request.prompt)
        validateResponse(request.response)

        val insightDTO = AiInsightDTO(
            id = 0, // Will be set by database
            userId = request.userId,
            budgetId = request.budgetId,
            budgetItemId = request.budgetItemId,
            prompt = request.prompt,
            response = request.response,
            type = request.type,
            sentiment = request.sentiment,
            metadata = request.metadata,
            createdAt = null // Will be set by database
        )

        return repository.create(insightDTO)
    }

    suspend fun updateInsight(id: Int, request: InsightUpdateRequest) {
        val existingInsight = validateInsightExists(id)

        request.prompt?.let { validatePrompt(it) }
        request.response?.let { validateResponse(it) }

        val updatedInsight = existingInsight.copy(
            prompt = request.prompt ?: existingInsight.prompt,
            response = request.response ?: existingInsight.response,
            type = request.type ?: existingInsight.type,
            sentiment = request.sentiment ?: existingInsight.sentiment,
            metadata = request.metadata ?: existingInsight.metadata
        )

        repository.update(id, updatedInsight)
    }

    suspend fun updateInsightSentiment(id: Int, sentiment: Sentiment?) {
        validateInsightExists(id)
        repository.updateSentiment(id, sentiment)
    }

    suspend fun updateInsightMetadata(id: Int, metadata: JsonElement?) {
        validateInsightExists(id)
        repository.updateMetadata(id, metadata)
    }

    // Delete Methods
    suspend fun deleteInsight(id: Int) {
        validateInsightExists(id)
        repository.delete(id)
    }

    suspend fun deleteUserInsights(userId: Int) {
        repository.deleteByUserId(userId)
    }

    suspend fun deleteBudgetInsights(budgetId: Int) {
        repository.deleteByBudgetId(budgetId)
    }

    suspend fun deleteBudgetItemInsights(budgetItemId: Int) {
        repository.deleteByBudgetItemId(budgetItemId)
    }
}