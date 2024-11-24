package com.budgetai.repositories

import com.budgetai.models.SavingsGoalDTO
import com.budgetai.models.SavingsGoals
import com.budgetai.models.Users
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SavingsGoalRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: SavingsGoalRepository
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(Users, SavingsGoals)
        }

        repository = SavingsGoalRepository(database)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(SavingsGoals, Users)
        }
        dbFile.delete()
    }

    private fun createSampleGoal(
        name: String = "Test Goal",
        targetAmount: Double = 1000.0,
        currentAmount: Double = 0.0,
        userId: Int = 1
    ): SavingsGoalDTO {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val targetDate = today.plus(kotlinx.datetime.DatePeriod(months = 6))

        return SavingsGoalDTO(
            id = 0,
            userId = userId,
            name = name,
            description = "Test Description",
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate.toString(),
            createdAt = ""
        )
    }

    @Test
    fun `test create and find savings goal by id`() = runBlocking {
        // Given
        val goal = createSampleGoal()

        // When
        val createdId = repository.create(goal)
        val retrievedGoal = repository.findById(createdId)

        // Then
        assertNotNull(retrievedGoal)
        assertEquals(goal.name, retrievedGoal.name)
        assertEquals(goal.targetAmount, retrievedGoal.targetAmount)
        assertEquals(goal.currentAmount, retrievedGoal.currentAmount)
    }

    @Test
    fun `test find goals by user id`() = runBlocking {
        // Given
        val userId = 1
        val goal1 = createSampleGoal("Goal 1", userId = userId)
        val goal2 = createSampleGoal("Goal 2", userId = userId)

        // When
        repository.create(goal1)
        repository.create(goal2)
        val userGoals = repository.findByUserId(userId)

        // Then
        assertEquals(2, userGoals.size)
        assertEquals(setOf("Goal 1", "Goal 2"), userGoals.map { it.name }.toSet())
    }

    @Test
    fun `test find active and completed goals`() = runBlocking {
        // Given
        val userId = 1
        val activeGoal = createSampleGoal("Active Goal", targetAmount = 1000.0, currentAmount = 500.0)
        val completedGoal = createSampleGoal("Completed Goal", targetAmount = 1000.0, currentAmount = 1000.0)

        // When
        repository.create(activeGoal)
        repository.create(completedGoal)
        val activeGoals = repository.findActiveByUserId(userId)
        val completedGoals = repository.findCompletedByUserId(userId)

        // Then
        assertEquals(1, activeGoals.size)
        assertEquals("Active Goal", activeGoals[0].name)
        assertEquals(1, completedGoals.size)
        assertEquals("Completed Goal", completedGoals[0].name)
    }

    @Test
    fun `test calculate progress`() = runBlocking {
        // Given
        val goal = createSampleGoal(targetAmount = 1000.0, currentAmount = 250.0)

        // When
        val createdId = repository.create(goal)
        val progress = repository.calculateProgress(createdId)

        // Then
        assertEquals(25.0, progress)
    }

    @Test
    fun `test update current amount operations`() = runBlocking {
        // Given
        val goal = createSampleGoal(targetAmount = 1000.0, currentAmount = 200.0)
        val createdId = repository.create(goal)

        // When - Test different amount operations
        repository.addToCurrentAmount(createdId, 300.0)
        var updatedGoal = repository.findById(createdId)
        assertEquals(500.0, updatedGoal?.currentAmount)

        repository.subtractFromCurrentAmount(createdId, 100.0)
        updatedGoal = repository.findById(createdId)
        assertEquals(400.0, updatedGoal?.currentAmount)

        repository.updateCurrentAmount(createdId, 600.0)
        updatedGoal = repository.findById(createdId)
        assertEquals(600.0, updatedGoal?.currentAmount)
    }

    @Test
    fun `test get total savings`() = runBlocking {
        // Given
        val userId = 1
        val goal1 = createSampleGoal(currentAmount = 500.0, userId = userId)
        val goal2 = createSampleGoal(currentAmount = 750.0, userId = userId)

        // When
        repository.create(goal1)
        repository.create(goal2)
        val totalSavings = repository.getTotalSavings(userId)

        // Then
        assertEquals(1250.0, totalSavings)
    }

    @Test
    fun `test goal tracking calculations`() = runBlocking {
        // Given
        val goal = createSampleGoal(targetAmount = 1000.0, currentAmount = 600.0)
        val createdId = repository.create(goal)

        // When
        val isOnTrack = repository.isGoalOnTrack(createdId)
        val remainingAmount = repository.getRemainingAmount(createdId)
        val dailySavings = repository.getRequiredDailySavings(createdId)

        // Then
        assertTrue(remainingAmount > 0)
        assertTrue(dailySavings > 0)
    }

    @Test
    fun `test delete operations`() = runBlocking {
        // Given
        val userId = 1
        val goal1 = createSampleGoal("Goal 1", userId = userId)
        val goal2 = createSampleGoal("Goal 2", userId = userId)

        // When
        val id1 = repository.create(goal1)
        repository.create(goal2)

        // Test single deletion
        repository.delete(id1)
        val deletedGoal = repository.findById(id1)
        assertNull(deletedGoal)

        // Test deletion by user ID
        repository.deleteByUserId(userId)
        val remainingGoals = repository.findByUserId(userId)
        assertTrue(remainingGoals.isEmpty())
    }

    @Test
    fun `test update savings goal`() = runBlocking {
        // Given
        val initialGoal = createSampleGoal("Initial Name")

        // When
        val createdId = repository.create(initialGoal)
        val updatedGoal = initialGoal.copy(
            id = createdId,
            name = "Updated Name",
            targetAmount = 2000.0
        )
        repository.update(createdId, updatedGoal)
        val retrievedGoal = repository.findById(createdId)

        // Then
        assertNotNull(retrievedGoal)
        assertEquals("Updated Name", retrievedGoal.name)
        assertEquals(2000.0, retrievedGoal.targetAmount)
    }
}