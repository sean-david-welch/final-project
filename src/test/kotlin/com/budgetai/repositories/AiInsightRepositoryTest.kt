package com.budgetai.repositories

import com.budgetai.models.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiInsightRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: AiInsightRepository
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        transaction(database) {
            SchemaUtils.create(Users, Budgets, BudgetItems, AiInsights)
        }

        repository = AiInsightRepository(database)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(AiInsights, BudgetItems, Budgets, Users)
        }
        dbFile.delete()
    }

    private fun createSampleInsight(
        userId: Int = 1,
        budgetId: Int = 1,
        budgetItemId: Int? = null,
        type: InsightType = InsightType.SPENDING_PATTERN,
        sentiment: Sentiment = Sentiment.NEUTRAL
    ): AiInsightDTO {
        val metadata = JsonObject(mapOf("key" to JsonPrimitive("value")))

        return AiInsightDTO(
            id = 0,
            userId = userId,
            budgetId = budgetId,
            budgetItemId = budgetItemId,
            prompt = "Sample prompt",
            response = "Sample response",
            type = type,
            sentiment = sentiment,
            metadata = metadata,
            createdAt = ""
        )
    }

    @Test
    fun `test create and find insight by id`() = runBlocking {
        // Given
        val insight = createSampleInsight()

        // When
        val createdId = repository.create(insight)
        val retrievedInsight = repository.findById(createdId)

        // Then
        assertNotNull(retrievedInsight)
        assertEquals(insight.prompt, retrievedInsight.prompt)
        assertEquals(insight.response, retrievedInsight.response)
        assertEquals(insight.type, retrievedInsight.type)
        assertEquals(insight.sentiment, retrievedInsight.sentiment)
    }

    @Test
    fun `test find insights by user id`() = runBlocking {
        // Given
        val userId = 1
        val insight1 = createSampleInsight(userId = userId, type = InsightType.SPENDING_PATTERN)
        val insight2 = createSampleInsight(userId = userId, type = InsightType.BUDGET_SUGGESTION)

        // When
        repository.create(insight1)
        repository.create(insight2)
        val userInsights = repository.findByUserId(userId)

        // Then
        assertEquals(2, userInsights.size)
        assertEquals(setOf(InsightType.SPENDING_PATTERN, InsightType.BUDGET_SUGGESTION),
                    userInsights.map { it.type }.toSet())
    }

    @Test
    fun `test find insights by date range`() = runBlocking {
        // Given
        val userId = 1
        val startDate = LocalDateTime(2024, 1, 1, 0, 0)
        val endDate = LocalDateTime(2024, 12, 31, 23, 59)

        // When
        repository.create(createSampleInsight(userId = userId))
        val insights = repository.findByUserIdAndDateRange(userId, startDate, endDate)

        // Then
        assertTrue(insights.isNotEmpty())
    }

    @Test
    fun `test find insights by type and sentiment`() = runBlocking {
        // Given
        val insight1 = createSampleInsight(type = InsightType.SPENDING_PATTERN, sentiment = Sentiment.POSITIVE)
        val insight2 = createSampleInsight(type = InsightType.SPENDING_PATTERN, sentiment = Sentiment.NEGATIVE)

        // When
        repository.create(insight1)
        repository.create(insight2)
        val typeInsights = repository.findByType(InsightType.SPENDING_PATTERN)
        val sentimentInsights = repository.findBySentiment(Sentiment.POSITIVE)

        // Then
        assertEquals(2, typeInsights.size)
        assertEquals(1, sentimentInsights.size)
    }

    @Test
    fun `test update insight`() = runBlocking {
        // Given
        val initialInsight = createSampleInsight()

        // When
        val createdId = repository.create(initialInsight)
        val updatedInsight = initialInsight.copy(
            id = createdId,
            prompt = "Updated prompt",
            sentiment = Sentiment.POSITIVE
        )
        repository.update(createdId, updatedInsight)
        val retrievedInsight = repository.findById(createdId)

        // Then
        assertNotNull(retrievedInsight)
        assertEquals("Updated prompt", retrievedInsight.prompt)
        assertEquals(Sentiment.POSITIVE, retrievedInsight.sentiment)
    }

    @Test
    fun `test update sentiment and metadata`() = runBlocking {
        // Given
        val insight = createSampleInsight()
        val createdId = repository.create(insight)

        // When - Update sentiment
        repository.updateSentiment(createdId, Sentiment.POSITIVE)
        var retrievedInsight = repository.findById(createdId)
        assertEquals(Sentiment.POSITIVE, retrievedInsight?.sentiment)

        // When - Update metadata
        val newMetadata = JsonObject(mapOf("newKey" to JsonPrimitive("newValue")))
        repository.updateMetadata(createdId, newMetadata)
        retrievedInsight = repository.findById(createdId)
        assertEquals(newMetadata, retrievedInsight?.metadata)
    }

    @Test
    fun `test delete operations`() = runBlocking {
        // Given
        val userId = 1
        val budgetId = 1
        val insight1 = createSampleInsight(userId = userId, budgetId = budgetId)
        val insight2 = createSampleInsight(userId = userId, budgetId = budgetId)

        // When - Test single deletion
        val id1 = repository.create(insight1)
        repository.create(insight2)
        repository.delete(id1)
        val deletedInsight = repository.findById(id1)
        assertNull(deletedInsight)

        // When - Test deletion by user ID
        repository.deleteByUserId(userId)
        val userInsights = repository.findByUserId(userId)
        assertTrue(userInsights.isEmpty())
    }

    @Test
    fun `test analysis methods`() = runBlocking {
        // Given
        val userId = 1
        repository.create(createSampleInsight(userId = userId, type = InsightType.SPENDING_PATTERN, sentiment = Sentiment.POSITIVE))
        repository.create(createSampleInsight(userId = userId, type = InsightType.BUDGET_SUGGESTION, sentiment = Sentiment.NEUTRAL))
        repository.create(createSampleInsight(userId = userId, type = InsightType.SPENDING_PATTERN, sentiment = Sentiment.NEGATIVE))

        // When - Test type distribution
        val typeDistribution = repository.getInsightTypeDistribution(userId)
        assertEquals(2, typeDistribution[InsightType.SPENDING_PATTERN])
        assertEquals(1, typeDistribution[InsightType.BUDGET_SUGGESTION])

        // When - Test sentiment distribution
        val sentimentDistribution = repository.getSentimentDistribution(userId)
        assertEquals(1, sentimentDistribution[Sentiment.POSITIVE])
        assertEquals(1, sentimentDistribution[Sentiment.NEUTRAL])
        assertEquals(1, sentimentDistribution[Sentiment.NEGATIVE])
    }

    @Test
    fun `test get recent insights with pagination`() = runBlocking {
        // Given
        val userId = 1
        repeat(15) {
            repository.create(createSampleInsight(userId = userId))
        }

        // When
        val firstPage = repository.getRecentInsights(userId, limit = 10, offset = 0)
        val secondPage = repository.getRecentInsights(userId, limit = 10, offset = 10)

        // Then
        assertEquals(10, firstPage.size)
        assertEquals(5, secondPage.size)
    }

    @Test
    fun `test find by budget and budget item`() = runBlocking {
        // Given
        val budgetId = 1
        val budgetItemId = 1
        repository.create(createSampleInsight(budgetId = budgetId, budgetItemId = budgetItemId))
        repository.create(createSampleInsight(budgetId = budgetId, budgetItemId = null))

        // When
        val budgetInsights = repository.findByBudgetId(budgetId)
        val itemInsights = repository.findByBudgetItemId(budgetItemId)

        // Then
        assertEquals(2, budgetInsights.size)
        assertEquals(1, itemInsights.size)
    }
}