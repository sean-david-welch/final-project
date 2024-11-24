package com.budgetai.services

import com.budgetai.models.AiInsights
import com.budgetai.models.InsightType
import com.budgetai.models.Sentiment
import com.budgetai.repositories.AiInsightRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AiInsightServiceTest {
    private lateinit var database: Database
    private lateinit var repository: AiInsightRepository
    private lateinit var service: AiInsightService
    private val dbFile = File("test.db")

    private val testUserId = 1
    private val testBudgetId = 1
    private val testBudgetItemId = 1
    private val testPrompt = "Analyze my spending patterns"
    private val testResponse = "Based on your spending patterns, you tend to spend more on weekends."
    private val testMetadata = JsonObject(mapOf("key" to JsonPrimitive("value")))

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(AiInsights)
        }

        repository = AiInsightRepository(database)
        service = AiInsightService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(AiInsights)
        }
        dbFile.delete()
    }

    @Test
    fun `createInsight should create insight with valid request`() = runBlocking {
        // Given
        val request = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            budgetItemId = testBudgetItemId,
            prompt = testPrompt,
            response = testResponse,
            type = InsightType.ITEM_ANALYSIS,
            sentiment = Sentiment.NEUTRAL,
            metadata = testMetadata
        )

        // When
        val insightId = service.createInsight(request)
        val createdInsight = service.getInsight(insightId)

        // Then
        assertNotNull(createdInsight)
        assertEquals(request.prompt, createdInsight.prompt)
        assertEquals(request.response, createdInsight.response)
        assertEquals(request.type, createdInsight.type)
        assertEquals(request.sentiment, createdInsight.sentiment)
    }

    @Test
    fun `createInsight should throw exception for empty prompt`(): Unit = runBlocking {
        // Given
        val request = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            prompt = "",
            response = testResponse,
            type = InsightType.ITEM_ANALYSIS
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createInsight(request)
        }
    }

    @Test
    fun `createInsight should throw exception for long response`(): Unit = runBlocking {
        // Given
        val request = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            prompt = testPrompt,
            response = "a".repeat(5001),
            type = InsightType.ITEM_ANALYSIS
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createInsight(request)
        }
    }

    @Test
    fun `updateInsight should update insight details correctly`() = runBlocking {
        // Given
        val createRequest = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            prompt = testPrompt,
            response = testResponse,
            type = InsightType.ITEM_ANALYSIS
        )
        val insightId = service.createInsight(createRequest)

        val updateRequest = AiInsightService.InsightUpdateRequest(
            prompt = "Updated prompt", response = "Updated response", type = InsightType.SAVING_SUGGESTION
        )

        // When
        service.updateInsight(insightId, updateRequest)
        val updatedInsight = service.getInsight(insightId)

        // Then
        assertEquals(updateRequest.prompt, updatedInsight?.prompt)
        assertEquals(updateRequest.response, updatedInsight?.response)
        assertEquals(updateRequest.type, updatedInsight?.type)
    }

    @Test
    fun `updateInsightSentiment should update sentiment correctly`() = runBlocking {
        // Given
        val createRequest = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            prompt = testPrompt,
            response = testResponse,
            type = InsightType.ITEM_ANALYSIS
        )
        val insightId = service.createInsight(createRequest)

        // When
        service.updateInsightSentiment(insightId, Sentiment.POSITIVE)
        val updatedInsight = service.getInsight(insightId)

        // Then
        assertEquals(Sentiment.POSITIVE, updatedInsight?.sentiment)
    }

    @Test
    fun `getUserInsightAnalytics should return correct analytics`() = runBlocking {
        // Given
        val requests = listOf(
            AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.ITEM_ANALYSIS,
                sentiment = Sentiment.POSITIVE
            ), AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.SAVING_SUGGESTION,
                sentiment = Sentiment.NEUTRAL
            )
        )
        requests.forEach { service.createInsight(it) }

        // When
        val analytics = service.getUserInsightAnalytics(testUserId)

        // Then
        assertEquals(2, analytics.totalInsights)
        assertEquals(1, analytics.typeDistribution[InsightType.ITEM_ANALYSIS])
        assertEquals(1, analytics.typeDistribution[InsightType.SAVING_SUGGESTION])
        assertEquals(1, analytics.sentimentDistribution[Sentiment.POSITIVE])
        assertEquals(1, analytics.sentimentDistribution[Sentiment.NEUTRAL])
    }

    @Test
    fun `getRecentInsightsPaginated should return correct page size`() = runBlocking {
        // Given
        val requests = List(15) {
            AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.ITEM_ANALYSIS
            )
        }
        requests.forEach { service.createInsight(it) }

        // When
        val firstPage = service.getRecentInsightsPaginated(testUserId, page = 0, pageSize = 10)
        val secondPage = service.getRecentInsightsPaginated(testUserId, page = 1, pageSize = 10)

        // Then
        assertEquals(10, firstPage.size)
        assertEquals(5, secondPage.size)
    }

    @Test
    fun `getInsightsInDateRange should return insights in correct range`() = runBlocking {
        // Given
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val startDate = now.minus(kotlinx.datetime.DatePeriod(days = 1))
        val endDate = now.plus(kotlinx.datetime.DatePeriod(days = 1))

        val request = AiInsightService.InsightCreationRequest(
            userId = testUserId,
            budgetId = testBudgetId,
            prompt = testPrompt,
            response = testResponse,
            type = InsightType.ITEM_ANALYSIS
        )
        service.createInsight(request)

        // When
        val insights = service.getInsightsInDateRange(testUserId, startDate, endDate)

        // Then
        assertEquals(1, insights.size)
    }

    @Test
    fun `deleteUserInsights should remove all user insights`() = runBlocking {
        // Given
        val requests = List(3) {
            AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.ITEM_ANALYSIS
            )
        }
        requests.forEach { service.createInsight(it) }

        // When
        service.deleteUserInsights(testUserId)
        val remainingInsights = service.getUserInsights(testUserId)

        // Then
        assertTrue(remainingInsights.isEmpty())
    }

    @Test
    fun `getInsightsByType should return correct insights`() = runBlocking {
        // Given
        val requests = listOf(
            AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.ITEM_ANALYSIS
            ), AiInsightService.InsightCreationRequest(
                userId = testUserId,
                budgetId = testBudgetId,
                prompt = testPrompt,
                response = testResponse,
                type = InsightType.SAVING_SUGGESTION
            )
        )
        requests.forEach { service.createInsight(it) }

        // When
        val spendingPatternInsights = service.getInsightsByType(InsightType.ITEM_ANALYSIS)
        val budgetRecommendationInsights = service.getInsightsByType(InsightType.SAVING_SUGGESTION)

        // Then
        assertEquals(1, spendingPatternInsights.size)
        assertEquals(1, budgetRecommendationInsights.size)
    }
}