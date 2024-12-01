package com.budgetai.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.budgetai.models.*
import com.budgetai.plugins.configureRouting
import com.budgetai.plugins.configureSecurity
import com.budgetai.plugins.configureSerialization
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
import java.util.*
import kotlin.test.assertEquals

class SavingsGoalRoutesTest {
    private lateinit var database: Database
    private val dbFile = File("test.db")

    // Test JWT constants
    private val testSecret = "your-test-secret-key"
    private val testIssuer = "http://0.0.0.0:8080/"
    private val testAudience = "http://0.0.0.0:8080/hello"
    private val testRealm = "Access to 'hello'"

    private val testConfig = ConfigFactory.parseString(
        """
        jwt {
            secret = "$testSecret"
            issuer = "$testIssuer"
            audience = "$testAudience"
            realm = "$testRealm"
        }
    """
    )

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
        application {
            val config = HoconApplicationConfig(testConfig)
            configureSecurity(config)
            configureSerialization()
            configureRouting(config, database)
        }

        val token = createTestJwtToken()

        val response = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            // Set the JWT as a cookie instead of header
            cookie("jwt_token", token)
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1,
                        name = "New Car",
                        targetAmount = 20000.0,
                        targetDate = "2024-12-31",
                        description = "Saving for a new car"
                    )
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    private fun createTestJwtToken(): String {
        return JWT.create().withAudience(testAudience).withIssuer(testIssuer).withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .withClaim("userId", 1).withClaim("role", "user").sign(HMAC256(testSecret))
    }

    @Test
    fun `GET savings-goal - returns goal when exists`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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

        val response = client.get("/api/savings-goals/$goalId")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET goal progress - returns correct progress`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal with known amounts
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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

        val response = client.get("/api/savings-goals/$goalId/progress")
        assertEquals(HttpStatusCode.OK, response.status)

        val progress = Json.decodeFromString<GoalProgress>(response.bodyAsText())
        assertEquals(25.0, progress.percentageComplete) // 5000/20000 = 25%
    }

    @Test
    fun `GET user savings goals - returns all goals for user`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create multiple goals for the same user
        repeat(3) {
            client.post("/api/savings-goals") {
                contentType(ContentType.Application.Json)
                setBody(
                    Json.encodeToString(
                        SavingsGoalCreationRequest(
                            userId = 1, name = "Goal $it", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal $it"
                        )
                    )
                )
            }
        }

        val response = client.get("/api/savings-goals/user/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val goals = Json.decodeFromString<List<SavingsGoalDTO>>(response.bodyAsText())
        assertEquals(3, goals.size)
    }

    @Test
    fun `GET active savings goals - returns only active goals`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create goals with different statuses
        client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Active Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Active goal"
                    )
                )
            )
        }

        val response = client.get("/api/savings-goals/user/1/active")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT savings-goal - updates successfully`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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
            setBody(Json.encodeToString(ContributionRequest(amount = 500.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST withdraw - processes withdrawal successfully`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal with initial amount
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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
            setBody(Json.encodeToString(WithdrawalRequest(amount = 200.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT current-amount - updates amount successfully`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
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
            setBody(Json.encodeToString(UpdateCurrentAmountRequest(amount = 750.0)))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE savings-goal - deletes successfully`() = testApplication {
        application {
            val config = HoconApplicationConfig(ConfigFactory.load())

            configureSerialization()
            configureRouting(config, database)
        }

        // Create a goal first
        val createResponse = client.post("/api/savings-goals") {
            contentType(ContentType.Application.Json)
            setBody(
                Json.encodeToString(
                    SavingsGoalCreationRequest(
                        userId = 1, name = "Test Goal", targetAmount = 1000.0, targetDate = "2024-12-31", description = "Test goal"
                    )
                )
            )
        }
        val goalId = Json.decodeFromString<Map<String, Int>>(createResponse.bodyAsText())["id"]

        val deleteResponse = client.delete("/api/savings-goals/$goalId")
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/api/savings-goals/$goalId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}