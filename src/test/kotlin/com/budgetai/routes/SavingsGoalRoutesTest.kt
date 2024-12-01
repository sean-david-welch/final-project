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

class SavingsGoalRoutesTest : AuthenticatedTest() {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(Users, SavingsGoals)
        }
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(SavingsGoals, Users)
        }
        dbFile.delete()
    }

    @Test
    fun `POST savings-goal - creates goal successfully`() = testApplication {
        configureTestApplication(database)

        val response = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "New Car", targetAmount = 20000.0, targetDate = "2024-12-31",
                        description = "Saving for a new car"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `GET savings-goal - returns goal when exists`() = testApplication {
        configureTestApplication(database)

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "New Car", targetAmount = 20000.0, targetDate = "2024-12-31",
                        description = "Saving for a new car"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/api/savings-goals/$goalId") {
            withAuth()
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET goal progress - returns correct progress`() = testApplication {
        configureTestApplication(database)

        // Create a goal with known amounts
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "New Car", targetAmount = 20000.0, initialAmount = 5000.0, targetDate = "2024-12-31",
                        description = "Saving for a new car"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.get("/api/savings-goals/$goalId/progress") {
            withAuth()
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val progress = Json.decodeFromString<GoalProgress>(response.bodyAsText())
        assertEquals(25.0, progress.percentageComplete)
    }

    @Test
    fun `GET user savings goals - returns all goals for user`() = testApplication {
        configureTestApplication(database)

        // Create multiple goals for the same user
        repeat(3) {
            client.post("/api/savings-goals") {
                contentType(ContentType.Application.Json)
                withAuth()
                setBody(
                    Json.encodeToString(
                        SavingsGoalCreationRequest(
                            userId = 1, name = "Goal $it", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal $it"
                        )
                    )
                )
            }
        }

        val response = client.get("/api/savings-goals/user/1") {
            withAuth()
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val goals = Json.decodeFromString<List<SavingsGoalDTO>>(response.bodyAsText())
        assertEquals(3, goals.size)
    }

    @Test
    fun `GET active savings goals - returns only active goals`() = testApplication {
        configureTestApplication(database)

        client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Active Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Active goal"
                    )
                )
            )
        }

        val response = client.get("/api/savings-goals/user/1/active") {
            withAuth()
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT savings-goal - updates successfully`() = testApplication {
        configureTestApplication(database)

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Original Goal", targetAmount = 1000.0, targetDate = "2024-12-31",
                        description = "Original description"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/api/savings-goals/$goalId") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalUpdateRequest(
                        name = "Updated Goal", targetAmount = 2000.0, targetDate = "2025-12-31", description = "Updated description"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST contribute - adds contribution successfully`() = testApplication {
        configureTestApplication(database)

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Test Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.post("/api/savings-goals/$goalId/contribute") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(Json.encodeToString(ContributionRequest(amount = 500.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST withdraw - processes withdrawal successfully`() = testApplication {
        configureTestApplication(database)

        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Test Goal", targetAmount = 1000.0, initialAmount = 200.0, targetDate = "2024-12-31",
                        description = "Test goal"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.post("/api/savings-goals/$goalId/withdraw") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(Json.encodeToString(WithdrawalRequest(amount = 200.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT current-amount - updates amount successfully`() = testApplication {
        configureTestApplication(database)

        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Test Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val response = client.put("/api/savings-goals/$goalId/current-amount") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(Json.encodeToString(UpdateCurrentAmountRequest(amount = 750.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE savings-goal - deletes successfully`() = testApplication {
        configureTestApplication(database)

        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            withAuth()
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Test Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val deleteResponse = client.delete("/api/savings-goals/$goalId") {
            withAuth()
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/api/savings-goals/$goalId") {
            withAuth()
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}