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
import kotlin.test.assertNotNull

class UserRoutesTest : AuthenticatedTest() {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Users)
        }
        dbFile.delete()
    }

    @Test
    fun `POST register - creates user successfully`() = testApplication {
        configureTestApplication(database)
        val userRequest = UserCreationRequest(
            email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
        )

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(Json.encodeToString(userRequest))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = Json.decodeFromString<Map<String, Int>>(response.bodyAsText())
        assertNotNull(responseBody["userId"])
    }

    @Test
    fun `POST register - fails with duplicate email`() = testApplication {
        configureTestApplication(database)
        // Create first user
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        // Try to create another user with same email
        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "password456", name = "Another User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST login - authenticates successfully`() = testApplication {
        configureTestApplication(database)
        // Create user first
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        // Try to login
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserAuthenticationRequest(
                        email = "test@example.com", password = "StrongPassword999"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET user - returns user when exists`() = testApplication {
        configureTestApplication(database)
        // Create user first
        val createResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }
        val responseData = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())
        val userId = responseData["userid"]

        val response = client.get("/api/users/$userId") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET user by email - returns user when exists`() = testApplication {
        configureTestApplication(database)
        // Create user first
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        val response = client.get("/api/users/email/test@example.com") { withAuth() }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT user - updates successfully`() = testApplication {
        configureTestApplication(database)
        // Create user first
        val createResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }
        val responseData = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())
        val userId = responseData["userid"]

        val response = client.put("/api/users/$userId") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UpdateUserRequest(
                        email = "updated@example.com", name = "Updated User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT password - updates successfully`() = testApplication {
        configureTestApplication(database)
        // Create user first
        val createResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }
        val responseData = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())
        val userId = responseData["userid"]

        val response = client.put("/api/users/$userId/password") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UpdatePasswordRequest(
                        newPassword = "StongerPassword987"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE user - deletes successfully`() = testApplication {
        configureTestApplication(database)

        // Create user first
        val createResponse = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    UserCreationRequest(
                        email = "test@example.com", password = "StrongPassword999", name = "Test User", role = UserRole.USER.toString()
                    )
                )
            )
        }

        // Parse the nested response structure
        val responseData = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())
        val userId = responseData["userid"]

        val deleteResponse = client.delete("/api/users/$userId") { withAuth() }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/api/users/$userId") { withAuth() }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}