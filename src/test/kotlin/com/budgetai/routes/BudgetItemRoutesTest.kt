package com.budgetai.routes

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

class BudgetItemRoutesTest {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users, Budgets, BudgetItems, Categories)
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
            SchemaUtils.drop(BudgetItems, Categories, Budgets, Users)
        }
        dbFile.delete()
    }

    @Test
    fun `POST budget-item - creates item successfully`() = testApplication {
        val response = client.post("/api/budget-items") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemCreationRequest(
                        budgetId = 1,
                        categoryId = 1,
                        name = "Test Item",
                        amount = 100.0,
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST bulk budget-items - creates multiple items successfully`() = testApplication {
        val items = listOf(
            BudgetItemCreationRequest(
                budgetId = 1,
                categoryId = 1,
                name = "Item 1",
                amount = 100.0,
            ), BudgetItemCreationRequest(
                budgetId = 1,
                categoryId = 1,
                name = "Item 2",
                amount = 200.0,
            )
        )

        val response = client.post("/api/budget-items/bulk") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(items))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `GET budget-item - returns item when exists`() = testApplication {
        // Create an item first
        val createResponse = client.post("/api/budget-items") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemCreationRequest(
                        budgetId = 1,
                        categoryId = 1,
                        name = "Test Item",
                        amount = 100.0,
                    )
                )
            )
        }
        val itemId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/api/budget-items/$itemId")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET budget items by budget - returns all items for budget`() = testApplication {
        // Create two items for the same budget
        repeat(2) {
            client.post("/api/budget-items") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        BudgetItemCreationRequest(
                            budgetId = 1,
                            categoryId = 1,
                            name = "Test Item $it",
                            amount = 100.0,
                        )
                    )
                )
            }
        }

        val response = client.get("/api/budget-items/budget/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val items = Json.decodeFromString<List<BudgetItemDTO>>(response.bodyAsText())
        assertEquals(2, items.size)
    }

    @Test
    fun `GET budget total - calculates total correctly`() = testApplication {
        // Create two items with known amounts
        repeat(2) {
            client.post("/api/budget-items") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        BudgetItemCreationRequest(
                            budgetId = 1,
                            categoryId = 1,
                            name = "Test Item $it",
                            amount = 100.0,
                        )
                    )
                )
            }
        }

        val response = client.get("/api/budget-items/budget/1/total")
        assertEquals(HttpStatusCode.OK, response.status)

        val total = Json.decodeFromString<Map<String, Double>>(response.bodyAsText())["total"]
        assertEquals(200.0, total)
    }

    @Test
    fun `PUT budget-item - updates successfully`() = testApplication {
        // Create an item first
        val createResponse = client.post("/api/budget-items") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemCreationRequest(
                        budgetId = 1,
                        categoryId = 1,
                        name = "Test Item",
                        amount = 100.0,
                    )
                )
            )
        }
        val itemId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/api/budget-items/$itemId") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemUpdateRequest(
                        name = "Updated Item", amount = 150.0, categoryId = 1
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT budget-item amount - updates amount successfully`() = testApplication {
        // Create an item first
        val createResponse = client.post("/api/budget-items") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemCreationRequest(
                        budgetId = 1,
                        categoryId = 1,
                        name = "Test Item",
                        amount = 100.0,
                    )
                )
            )
        }
        val itemId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/api/budget-items/$itemId/amount") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UpdateAmountRequest(amount = 150.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE budget-item - deletes successfully`() = testApplication {
        // Create an item first
        val createResponse = client.post("/api/budget-items") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    BudgetItemCreationRequest(
                        budgetId = 1,
                        categoryId = 1,
                        name = "Test Item",
                        amount = 100.0,
                    )
                )
            )
        }
        val itemId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val deleteResponse = client.delete("/api/budget-items/$itemId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/api/budget-items/$itemId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `DELETE budget items - deletes all items for budget`() = testApplication {
        // Create two items for the same budget
        repeat(2) {
            client.post("/api/budget-items") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        BudgetItemCreationRequest(
                            budgetId = 1,
                            categoryId = 1,
                            name = "Test Item $it",
                            amount = 100.0,
                        )
                    )
                )
            }
        }

        val deleteResponse = client.delete("/api/budget-items/budget/1")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/api/budget-items/budget/1")
        val items = Json.decodeFromString<List<BudgetItemDTO>>(getResponse.bodyAsText())
        assertEquals(0, items.size)
    }
}