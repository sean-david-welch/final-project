package com.budgetai.routes

import com.budgetai.models.BudgetDTO
import com.budgetai.models.Budgets
import com.budgetai.models.Users
import com.budgetai.plugins.configureRouting
import com.budgetai.services.BudgetService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

class BudgetRoutesTest {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users, Budgets)
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
        // Set up
        application {
            configureRouting()  // Your main routing configuration
        }

        // Test
        val response = client.post("/budgets") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(BudgetService.BudgetCreationRequest(
                userId = 1,
                name = "Test Budget",
                description = "Test Description",
                startDate = null,
                endDate = null
            )))
        }

        // Verify
        assertEquals(HttpStatusCode.Created, response.status)
        // You could also verify the response body contains an ID
    }

    @Test
    fun `GET budget - returns budget when exists`() = testApplication {
        // Set up
        application {
            configureRouting()
        }

        // Create a budget first
        val createResponse = client.post("/budgets") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(BudgetService.BudgetCreationRequest(
                userId = 1,
                name = "Test Budget",
                description = "Test Description",
                startDate = null,
                endDate = null
            )))
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Test
        val response = client.get("/budgets/$budgetId")

        // Verify
        assertEquals(HttpStatusCode.OK, response.status)
        // You could also verify the response body contains correct budget data
    }

    @Test
    fun `GET budget - returns 404 when budget doesn't exist`() = testApplication {
        application {
            configureRouting()
        }

        val response = client.get("/budgets/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT budget totals - updates successfully`() = testApplication {
        application {
            configureRouting()
        }

        // Create a budget first
        val createResponse = client.post("/budgets") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(BudgetService.BudgetCreationRequest(
                userId = 1,
                name = "Test Budget",
                description = "Test Description",
                startDate = null,
                endDate = null
            )))
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Update the totals
        val response = client.put("/budgets/$budgetId/totals") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateBudgetTotalsRequest(
                totalIncome = 1000.0,
                totalExpenses = 500.0
            )))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET budgets by user - returns all user budgets`() = testApplication {
        application {
            configureRouting()
        }

        // Create two budgets for the same user
        repeat(2) {
            client.post("/budgets") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(BudgetService.BudgetCreationRequest(
                    userId = 1,
                    name = "Test Budget $it",
                    description = "Test Description",
                    startDate = null,
                    endDate = null
                )))
            }
        }

        // Get budgets for user
        val response = client.get("/budgets/user/1")
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse response and verify count
        val budgets = Json.decodeFromString<List<BudgetDTO>>(response.bodyAsText())
        assertEquals(2, budgets.size)
    }

    @Test
    fun `DELETE budget - deletes successfully`() = testApplication {
        application {
            configureRouting()
        }

        // Create a budget first
        val createResponse = client.post("/budgets") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(BudgetService.BudgetCreationRequest(
                userId = 1,
                name = "Test Budget",
                description = "Test Description",
                startDate = null,
                endDate = null
            )))
        }
        val budgetId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Delete the budget
        val deleteResponse = client.delete("/budgets/$budgetId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify it's gone
        val getResponse = client.get("/budgets/$budgetId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}