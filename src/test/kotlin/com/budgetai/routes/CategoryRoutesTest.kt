package com.budgetai.routes

import com.budgetai.AuthenticatedTest
import com.budgetai.models.*
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

class CategoryRoutesTest : AuthenticatedTest() {
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
        configureTestApplication(database)
        val response = client.post("/api/categories") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Groceries", type = CategoryType.FIXED.toString(), description = "Food and household items"
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
        configureTestApplication(database)
        // Create two categories first
        repeat(2) { index ->
            client.post("/api/categories") {
                contentType(ContentType.Application.Json)
                withAuth()
                setBody(
                    Json.encodeToString(
                        CategoryCreationRequest(
                            name = "Category $index", type = CategoryType.FIXED.toString(), description = "Test Description"
                        )
                    )
                )
            }
        }

        val response = client.get("/api/categories") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)
        val categories = Json.decodeFromString<List<CategoryDTO>>(response.bodyAsText())
        assertEquals(2, categories.size)
    }

    @Test
    fun `GET category by ID - returns category when exists`() = testApplication {
        configureTestApplication(database)
        // Create a category first
        val createResponse = client.post("/api/categories") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Test Category", type = CategoryType.FIXED.toString(), description = "Test Description"
                    )
                )
            )
        }
        val categoryId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/api/categories/$categoryId") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET category by ID - returns 404 when category doesn't exist`() = testApplication {
        configureTestApplication(database)
        val response = client.get("/api/categories/999") { withAuth() }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET category by name - returns category when exists`() = testApplication {
        configureTestApplication(database)
        // Create a category first
        val categoryName = "Test Category"
        client.post("/api/categories") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = categoryName, type = CategoryType.FIXED.toString(), description = "Test Description"
                    )
                )
            )
        }

        val response = client.get("/api/categories/name/$categoryName") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE category - deletes successfully`() = testApplication {
        configureTestApplication(database)
        // Create a category first
        val createResponse = client.post("/api/categories") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    CategoryCreationRequest(
                        name = "Test Category", type = CategoryType.FIXED.toString(), description = "Test Description"
                    )
                )
            )
        }
        val categoryId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        // Delete the category
        val deleteResponse = client.delete("/api/categories/$categoryId") { withAuth() }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        // Verify it's gone
        val getResponse = client.get("/api/categories/$categoryId") { withAuth() }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}