package com.budgetai.routes

import com.budgetai.models.*
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class AiInsightRoutesTest {
    private lateinit var database: Database
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
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(AiInsights, BudgetItems, Budgets, Users)
        }
        dbFile.delete()
    }

    @Test
    fun `POST insight - creates insight successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        val response = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightCreationRequest(
                        userId = 1,
                        budgetId = 1,
                        type = InsightType.SPENDING_PATTERN,
                        content = "You've spent more on dining this month",
                        sentiment = Sentiment.NEUTRAL,
                        metadata = mapOf("category" to "dining")
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `GET insight - returns insight when exists`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insight first
        val createResponse = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightCreationRequest(
                        userId = 1,
                        budgetId = 1,
                        type = InsightType.SPENDING_PATTERN,
                        content = "Test insight",
                        sentiment = Sentiment.NEUTRAL,
                        metadata = mapOf("category" to "test")
                    )
                )
            )
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/ai-insights/$insightId")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET insights by type - returns filtered insights`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insights with different types
        repeat(2) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        InsightCreationRequest(
                            userId = 1,
                            budgetId = 1,
                            type = InsightType.SPENDING_PATTERN,
                            content = "Test insight $it",
                            sentiment = Sentiment.NEUTRAL,
                            metadata = mapOf("category" to "test")
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/type/SPENDING_PATTERN")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<InsightDTO>>(response.bodyAsText())
        assertEquals(2, insights.size)
        insights.forEach { assertEquals(InsightType.SPENDING_PATTERN, it.type) }
    }

    @Test
    fun `GET insights by sentiment - returns filtered insights`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insights with specific sentiment
        repeat(2) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        InsightCreationRequest(
                            userId = 1,
                            budgetId = 1,
                            type = InsightType.SPENDING_PATTERN,
                            content = "Test insight $it",
                            sentiment = Sentiment.POSITIVE,
                            metadata = mapOf("category" to "test")
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/sentiment/POSITIVE")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<InsightDTO>>(response.bodyAsText())
        insights.forEach { assertEquals(Sentiment.POSITIVE, it.sentiment) }
    }

    @Test
    fun `GET user insights - returns all user insights`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create multiple insights for user
        repeat(3) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        InsightCreationRequest(
                            userId = 1,
                            budgetId = 1,
                            type = InsightType.SPENDING_PATTERN,
                            content = "Test insight $it",
                            sentiment = Sentiment.NEUTRAL,
                            metadata = mapOf("category" to "test")
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/user/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<InsightDTO>>(response.bodyAsText())
        assertEquals(3, insights.size)
    }

    @Test
    fun `GET insights in date range - returns filtered insights`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create some insights
        repeat(2) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        InsightCreationRequest(
                            userId = 1,
                            budgetId = 1,
                            type = InsightType.SPENDING_PATTERN,
                            content = "Test insight $it",
                            sentiment = Sentiment.NEUTRAL,
                            metadata = mapOf("category" to "test")
                        )
                    )
                )
            }
        }

        val startDate = LocalDateTime(2024, 1, 1, 0, 0)
        val endDate = LocalDateTime(2024, 12, 31, 23, 59)

        val response = client.get("/ai-insights/user/1/date-range?startDate=$startDate&endDate=$endDate")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET recent insights paginated - returns correct page size`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create multiple insights
        repeat(15) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        InsightCreationRequest(
                            userId = 1,
                            budgetId = 1,
                            type = InsightType.SPENDING_PATTERN,
                            content = "Test insight $it",
                            sentiment = Sentiment.NEUTRAL,
                            metadata = mapOf("category" to "test")
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/user/1/recent?page=0&pageSize=10")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<InsightDTO>>(response.bodyAsText())
        assertEquals(10, insights.size)
    }

    @Test
    fun `PUT insight - updates successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insight first
        val createResponse = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightCreationRequest(
                        userId = 1,
                        budgetId = 1,
                        type = InsightType.SPENDING_PATTERN,
                        content = "Original content",
                        sentiment = Sentiment.NEUTRAL,
                        metadata = mapOf("category" to "test")
                    )
                )
            )
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/ai-insights/$insightId") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightUpdateRequest(
                        content = "Updated content",
                        type = InsightType.SAVINGS_GOAL,
                        sentiment = Sentiment.POSITIVE,
                        metadata = mapOf("category" to "updated")
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT sentiment - updates sentiment successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insight first
        val createResponse = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightCreationRequest(
                        userId = 1,
                        budgetId = 1,
                        type = InsightType.SPENDING_PATTERN,
                        content = "Test content",
                        sentiment = Sentiment.NEUTRAL,
                        metadata = mapOf("category" to "test")
                    )
                )
            )
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/ai-insights/$insightId/sentiment") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateSentimentRequest(sentiment = Sentiment.POSITIVE)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE insight - deletes successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insight first
        val createResponse = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightCreationRequest(
                        userId = 1,
                        budgetId = 1,
                        type = InsightType.SPENDING_PATTERN,
                        content = "Test content",
                        sentiment = Sentiment.NEUTRAL,
                        metadata = mapOf("category" to "test")
                    )
                )
            )
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val deleteResponse = client.delete("/ai-insights/$insightId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/ai-insights/$insightId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}