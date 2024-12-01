package com.budgetai.routes

import com.budgetai.AuthenticatedTest
import com.budgetai.models.*
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSerialization
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
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

class BudgetRoutesTest : AuthenticatedTest() {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users, Budgets)
        }
        TestApplication {
            val config = HoconApplicationConfig(ConfigFactory.load())
            application {
                configureSerialization()
                configureRouting(config, database)
            }
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Budgets, Users)
        }
        dbFile.delete()
    }

    @Test
    fun `POST budget - creates budget successfully`() = testApplication {
        configureTestApplication(database)
        // Test
        val response = client.post("/api/budgets") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    BudgetCreationRequest(
                        userId = 1,
                        name = "Test Budget",
                        description = "Test Description",
                        startDate = null,
                        endDate = null
                    )
                )
            )
        }

        // Verify
        assertEquals(HttpStatusCode.Created, response.status)
        // You could also verify the response body contains an ID
    }

    @Test
    fun `GET budget - returns budget when exists`() = testApplication {
        configureTestApplication(database)
        // Set up
        // Create a budget first
        val createResponse = client.post("/api/budgets") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    BudgetCreationRequest(
                        userId = 1,
                        name = "Test Budget",
                        description = "Test Description",
                        startDate = null,
                        endDate = null
                    )
                )
            )
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Test
        val response = client.get("/api/budgets/$budgetId") { withAuth() }

        // Verify
        assertEquals(HttpStatusCode.OK, response.status)
        // You could also verify the response body contains correct budget data
    }

    @Test
    fun `GET budget - returns 404 when budget doesn't exist`() = testApplication {
        configureTestApplication(database)
        val response = client.get("/api/budgets/999") { withAuth() }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT budget totals - updates successfully`() = testApplication {
        configureTestApplication(database)
        // Create a budget first
        val createResponse = client.post("/api/budgets") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    BudgetCreationRequest(
                        userId = 1,
                        name = "Test Budget",
                        description = "Test Description",
                        startDate = null,
                        endDate = null
                    )
                )
            )
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Update the totals
        val response = client.put("/api/budgets/$budgetId/totals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UpdateBudgetTotalsRequest(
                        totalIncome = 1000.0, totalExpenses = 500.0
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET budgets by user - returns all user budgets`() = testApplication {
        configureTestApplication(database)
        // Create two budgets for the same user
        repeat(2) {
            client.post("/api/budgets") {
                contentType(ContentType.Application.Json)
                withAuth()
                setBody(
                    Json.encodeToString(
                        BudgetCreationRequest(
                            userId = 1,
                            name = "Test Budget $it",
                            description = "Test Description",
                            startDate = null,
                            endDate = null
                        )
                    )
                )
            }
        }

        // Get budgets for user
        val response = client.get("/api/budgets/user/1") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse response and verify count
        val budgets = Json.decodeFromString<List<BudgetDTO>>(response.bodyAsText())
        assertEquals(2, budgets.size)
    }

    @Test
    fun `DELETE budget - deletes successfully`() = testApplication {
        configureTestApplication(database)
        // Create a budget first
        val createResponse = client.post("/api/budgets") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    BudgetCreationRequest(
                        userId = 1,
                        name = "Test Budget",
                        description = "Test Description",
                        startDate = null,
                        endDate = null
                    )
                )
            )
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Delete the budget
        val deleteResponse = client.delete("/api/budgets/$budgetId") { withAuth() }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify it's gone
        val getResponse = client.get("/api/budgets/$budgetId") { withAuth() }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}