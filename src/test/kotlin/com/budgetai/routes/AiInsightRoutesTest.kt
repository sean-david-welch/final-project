package com.budgetai.routes

import com.budgetai.models.*
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import kotlin.test.assertEquals

class AiInsightRoutesTest {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    // Sample test data for users, budgets, and budget items
    private fun setupTestData() = transaction(database) {
        // Create test user
        Users.insert {
            it[id] = 1
            it[email] = "test@example.com"
            it[passwordHash] = "hashedPassword123"
            it[name] = "Test User"
        }

        // Create test budget
        Budgets.insert {
            it[id] = 1
            it[userId] = 1
            it[name] = "Monthly Budget"
            it[description] = "Test budget for insights"
            it[startDate] = LocalDate(2024, 1, 1)
            it[endDate] = LocalDate(2024, 12, 31)
            it[totalIncome] = BigDecimal("5000.00")
            it[totalExpenses] = BigDecimal("3000.00")
        }

        // Create test category
        Categories.insert {
            it[id] = 1
            it[name] = "Dining"
            it[type] = CategoryType.EXPENSE
            it[description] = "Food and dining expenses"
        }

        // Create test budget item
        BudgetItems.insert {
            it[id] = 1
            it[budgetId] = 1
            it[categoryId] = 1
            it[name] = "Restaurant Budget"
            it[amount] = BigDecimal("300.00")
        }
    }

    private fun createSampleInsightRequest(
        userId: Int = 1,
        budgetId: Int = 1,
        budgetItemId: Int? = 1,
        type: InsightType = InsightType.SAVING_SUGGESTION,
        prompt: String = "Analyze my dining expenses for this month",
        response: String = "Your dining expenses have increased by 25% compared to last month."
    ) = InsightCreationRequest(userId = userId,
        budgetId = budgetId,
        budgetItemId = budgetItemId,
        prompt = prompt,
        response = response,
        type = type,
        metadata = buildJsonObject {
            put("categoryId", JsonPrimitive(1))
            put("categoryName", JsonPrimitive("Dining"))
            put("budgetedAmount", JsonPrimitive(300.00))
            put("currentSpend", JsonPrimitive(250.00))
            put("previousMonthSpend", JsonPrimitive(200.00))
            put("percentageChange", JsonPrimitive(25.00))
            put("remainingBudget", JsonPrimitive(50.00))
        })

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users, Categories, Budgets, BudgetItems, AiInsights)
            setupTestData() // Initialize required test data
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            // Drop tables in correct order to respect foreign key constraints
            SchemaUtils.drop(AiInsights, BudgetItems, Budgets, Categories, Users)
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
            setBody(Json.encodeToString(createSampleInsightRequest()))
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
            setBody(Json.encodeToString(createSampleInsightRequest()))
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

        // Create insights with specific type
        repeat(2) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        createSampleInsightRequest(
                            type = InsightType.SAVING_SUGGESTION
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/type/SAVING_SUGGESTION")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<AiInsightDTO>>(response.bodyAsText())
        assertEquals(2, insights.size)
        insights.forEach { assertEquals(InsightType.SAVING_SUGGESTION, it.type) }
    }

    @Test
    fun `GET insights by sentiment - returns filtered insights`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insights with specific sentiment
        val request = createSampleInsightRequest().copy(sentiment = Sentiment.POSITIVE)

        repeat(2) {
            client.post("/ai-insights") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(request))
            }
        }

        val response = client.get("/ai-insights/sentiment/POSITIVE")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<AiInsightDTO>>(response.bodyAsText())
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
                        createSampleInsightRequest(
                            prompt = "Analyze spend pattern $it", response = "Analysis result $it"
                        )
                    )
                )
            }
        }

        val response = client.get("/ai-insights/user/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val insights = Json.decodeFromString<List<AiInsightDTO>>(response.bodyAsText())
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
                setBody(Json.encodeToString(createSampleInsightRequest()))
            }
        }

        val startDate = LocalDateTime(2024, 1, 1, 0, 0)
        val endDate = LocalDateTime(2024, 12, 31, 23, 59)

        val response = client.get("/ai-insights/user/1/date-range?startDate=$startDate&endDate=$endDate")
        assertEquals(HttpStatusCode.OK, response.status)
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
            setBody(Json.encodeToString(createSampleInsightRequest()))
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val updatedMetadata = buildJsonObject {
            put("category", "updated_category")
            put("amount", 200.0)
        }

        val response = client.put("/ai-insights/$insightId") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    InsightUpdateRequest(
                        prompt = "Updated analysis request",
                        response = "Updated analysis result",
                        type = InsightType.ITEM_ANALYSIS,
                        sentiment = Sentiment.POSITIVE,
                        metadata = updatedMetadata
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
            setBody(Json.encodeToString(createSampleInsightRequest()))
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/ai-insights/$insightId/sentiment") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateSentimentRequest(sentiment = Sentiment.POSITIVE)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT metadata - updates metadata successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create insight first
        val createResponse = client.post("/ai-insights") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createSampleInsightRequest()))
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val newMetadata = buildJsonObject {
            put("category", "updated_category")
            put("amount", 300.0)
            put("note", "Updated via test")
        }

        val response = client.put("/ai-insights/$insightId/metadata") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateMetadataRequest(metadata = newMetadata)))
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
            setBody(Json.encodeToString(createSampleInsightRequest()))
        }
        val insightId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val deleteResponse = client.delete("/ai-insights/$insightId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/ai-insights/$insightId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}