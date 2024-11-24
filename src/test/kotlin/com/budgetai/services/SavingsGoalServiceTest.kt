package com.budgetai.services

import com.budgetai.repositories.SavingsGoalRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SavingsGoalServiceTest {
    private lateinit var database: Database
    private lateinit var repository: SavingsGoalRepository
    private lateinit var service: SavingsGoalService
    private val dbFile = File("test.db")
    private val userId = 1
    private val tomorrow = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(kotlinx.datetime.DatePeriod(days = 1)).toString()

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(SavingsGoals)
        }

        repository = SavingsGoalRepository(database)
        service = SavingsGoalService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(SavingsGoals)
        }
        dbFile.delete()
    }

    @Test
    fun `createSavingsGoal should create goal with valid request`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId,
            name = "New Car",
            description = "Saving for a Tesla",
            targetAmount = 50000.0,
            initialAmount = 1000.0,
            targetDate = tomorrow
        )

        // When
        val goalId = service.createSavingsGoal(request)
        val createdGoal = service.getSavingsGoal(goalId)

        // Then
        assertEquals(request.name, createdGoal?.name)
        assertEquals(request.description, createdGoal?.description)
        assertEquals(request.targetAmount, createdGoal?.targetAmount)
        assertEquals(request.initialAmount, createdGoal?.currentAmount)
    }

    @Test
    fun `createSavingsGoal should throw exception for negative target amount`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Invalid Goal", targetAmount = -1000.0
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createSavingsGoal(request)
        }
    }

    @Test
    fun `createSavingsGoal should throw exception for past target date`() = runBlocking {
        // Given
        val pastDate = "2020-01-01"
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Invalid Goal", targetAmount = 1000.0, targetDate = pastDate
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createSavingsGoal(request)
        }
    }

    @Test
    fun `addContribution should update current amount correctly`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Vacation", targetAmount = 5000.0, initialAmount = 1000.0
        )
        val goalId = service.createSavingsGoal(request)

        // When
        service.addContribution(goalId, 500.0)
        val updatedGoal = service.getSavingsGoal(goalId)

        // Then
        assertEquals(1500.0, updatedGoal?.currentAmount)
    }

    @Test
    fun `addContribution should throw exception when exceeding target amount`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Vacation", targetAmount = 5000.0, initialAmount = 4500.0
        )
        val goalId = service.createSavingsGoal(request)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.addContribution(goalId, 1000.0)
        }
    }

    @Test
    fun `withdrawAmount should update current amount correctly`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Vacation", targetAmount = 5000.0, initialAmount = 1000.0
        )
        val goalId = service.createSavingsGoal(request)

        // When
        service.withdrawAmount(goalId, 500.0)
        val updatedGoal = service.getSavingsGoal(goalId)

        // Then
        assertEquals(500.0, updatedGoal?.currentAmount)
    }

    @Test
    fun `withdrawAmount should throw exception when amount exceeds current balance`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Vacation", targetAmount = 5000.0, initialAmount = 1000.0
        )
        val goalId = service.createSavingsGoal(request)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.withdrawAmount(goalId, 2000.0)
        }
    }

    @Test
    fun `getGoalProgress should return correct progress information`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Vacation", targetAmount = 1000.0, initialAmount = 250.0, targetDate = tomorrow
        )
        val goalId = service.createSavingsGoal(request)

        // When
        val progress = service.getGoalProgress(goalId)

        // Then
        assertEquals(250.0, progress.currentAmount)
        assertEquals(1000.0, progress.targetAmount)
        assertEquals(25.0, progress.percentageComplete)
        assertEquals(750.0, progress.remainingAmount)
    }

    @Test
    fun `updateSavingsGoal should update goal details correctly`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Original Name", targetAmount = 1000.0
        )
        val goalId = service.createSavingsGoal(request)

        val updateRequest = SavingsGoalService.SavingsGoalUpdateRequest(
            name = "Updated Name", targetAmount = 2000.0
        )

        // When
        service.updateSavingsGoal(goalId, updateRequest)
        val updatedGoal = service.getSavingsGoal(goalId)

        // Then
        assertEquals("Updated Name", updatedGoal?.name)
        assertEquals(2000.0, updatedGoal?.targetAmount)
    }

    @Test
    fun `getUserSavingsGoals should return all user goals`() = runBlocking {
        // Given
        val requests = listOf(
            SavingsGoalService.SavingsGoalCreationRequest(
                userId = userId, name = "Goal 1", targetAmount = 1000.0
            ), SavingsGoalService.SavingsGoalCreationRequest(
                userId = userId, name = "Goal 2", targetAmount = 2000.0
            )
        )

        requests.forEach { service.createSavingsGoal(it) }

        // When
        val goals = service.getUserSavingsGoals(userId)

        // Then
        assertEquals(2, goals.size)
    }

    @Test
    fun `deleteSavingsGoal should remove goal`() = runBlocking {
        // Given
        val request = SavingsGoalService.SavingsGoalCreationRequest(
            userId = userId, name = "Temporary Goal", targetAmount = 1000.0
        )
        val goalId = service.createSavingsGoal(request)

        // When
        service.deleteSavingsGoal(goalId)
        val deletedGoal = service.getSavingsGoal(goalId)

        // Then
        assertNull(deletedGoal)
    }

    @Test
    fun `deleteUserSavingsGoals should remove all user goals`() = runBlocking {
        // Given
        val requests = listOf(
            SavingsGoalService.SavingsGoalCreationRequest(
                userId = userId, name = "Goal 1", targetAmount = 1000.0
            ), SavingsGoalService.SavingsGoalCreationRequest(
                userId = userId, name = "Goal 2", targetAmount = 2000.0
            )
        )

        requests.forEach { service.createSavingsGoal(it) }

        // When
        service.deleteUserSavingsGoals(userId)
        val remainingGoals = service.getUserSavingsGoals(userId)

        // Then
        assertTrue(remainingGoals.isEmpty())
    }
}