package com.budgetai.routes

import com.budgetai.models.*
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSerialization
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
import kotlin.test.assertTrue

class CategoryRoutesTest {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Categories)
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Categories)
        }
        dbFile.delete()
    }

    @Test
    fun `POST category - creates category successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        val response = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Groceries", type = CategoryType.EXPENSE, description = "Food and household items"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = Json.decodeFromString<Map<String, Int>>(response.bodyAsText())
        assertTrue(responseBody.containsKey("id"))
    }

    @Test
    fun `GET categories - returns all categories`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create two categories first
        repeat(2) { index ->
            client.post("/categories") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        CategoryCreationRequest(
                            name = "Category $index", type = CategoryType.EXPENSE, description = "Test Description"
                        )
                    )
                )
            }
        }

        val response = client.get("/categories")
        assertEquals(HttpStatusCode.OK, response.status)
        val categories = Json.decodeFromString<List<CategoryDTO>>(response.bodyAsText())
        assertEquals(2, categories.size)
    }

    @Test
    fun `GET category by ID - returns category when exists`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create a category first
        val createResponse = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Test Category", type = CategoryType.INCOME, description = "Test Description"
                    )
                )
            )
        }
        val categoryId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/categories/$categoryId")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET category by ID - returns 404 when category doesn't exist`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        val response = client.get("/categories/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET category by name - returns category when exists`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create a category first
        val categoryName = "Test Category"
        client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = categoryName, type = CategoryType.EXPENSE, description = "Test Description"
                    )
                )
            )
        }

        val response = client.get("/categories/name/$categoryName")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET categories by type - returns all categories of specified type`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create categories of different types
        client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Salary", type = CategoryType.INCOME, description = "Monthly salary"
                    )
                )
            )
        }

        client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Groceries", type = CategoryType.EXPENSE, description = "Food expenses"
                    )
                )
            )
        }

        val response = client.get("/categories/type/INCOME")
        assertEquals(HttpStatusCode.OK, response.status)
        val categories = Json.decodeFromString<List<CategoryDTO>>(response.bodyAsText())
        assertTrue(categories.all { it.type == CategoryType.INCOME })
    }

    @Test
    fun `PUT category - updates successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create a category first
        val createResponse = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Original Name", type = CategoryType.EXPENSE, description = "Original Description"
                    )
                )
            )
        }
        val categoryId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Update the category
        val response = client.put("/categories/$categoryId") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    UpdateCategoryRequest(
                        name = "Updated Name", type = CategoryType.INCOME, description = "Updated Description"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // Verify the update
        val getResponse = client.get("/categories/$categoryId")
        val updatedCategory = Json.decodeFromString<CategoryDTO>(getResponse.bodyAsText())
        assertEquals("Updated Name", updatedCategory.name)
        assertEquals(CategoryType.INCOME, updatedCategory.type)
        assertEquals("Updated Description", updatedCategory.description)
    }

    @Test
    fun `DELETE category - deletes successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting(database = database)
        }

        // Create a category first
        val createResponse = client.post("/categories") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Test Category", type = CategoryType.EXPENSE, description = "Test Description"
                    )
                )
            )
        }
        val categoryId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Delete the category
        val deleteResponse = client.delete("/categories/$categoryId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify it's gone
        val getResponse = client.get("/categories/$categoryId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}