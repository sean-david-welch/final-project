package com.budgetai.repositories

import com.budgetai.models.BudgetDTO
import com.budgetai.models.Budgets
import com.budgetai.models.Users
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BudgetRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: BudgetRepository
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
            SchemaUtils.create(Users, Budgets)
        }

        repository = BudgetRepository(database)
    }

    @After
    fun tearDown() {
        // Clean up tables after each test
        transaction(database) {
            SchemaUtils.drop(Budgets, Users)
        }
        // Delete the test database file
        dbFile.delete()
    }

    @Test
    fun `test create and find budget by id`() = runBlocking {
        // Given
        val budgetDTO = BudgetDTO(
            id = 0, // ID will be assigned by database
            userId = 1,
            name = "Test Budget",
            description = "Test Description",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            totalIncome = 1000.0,
            totalExpenses = 500.0,
            createdAt = "" // Will be set by database
        )

        // When
        val createdId = repository.create(budgetDTO)
        val retrievedBudget = repository.findById(createdId)

        // Then
        assertNotNull(retrievedBudget)
        assertEquals(budgetDTO.name, retrievedBudget.name)
        assertEquals(budgetDTO.totalIncome, retrievedBudget.totalIncome)
        assertEquals(budgetDTO.totalExpenses, retrievedBudget.totalExpenses)
    }

    @Test
    fun `test find budgets by user id`() = runBlocking {
        // Given
        val userId = 1
        val budget1 = BudgetDTO(
            id = 0,
            userId = userId,
            name = "Budget 1",
            description = "Description 1",
            startDate = "2024-01-01",
            endDate = "2024-06-30",
            totalIncome = 1000.0,
            totalExpenses = 500.0,
            createdAt = ""
        )
        val budget2 = BudgetDTO(
            id = 0,
            userId = userId,
            name = "Budget 2",
            description = "Description 2",
            startDate = "2024-07-01",
            endDate = "2024-12-31",
            totalIncome = 2000.0,
            totalExpenses = 1000.0,
            createdAt = ""
        )

        // When
        repository.create(budget1)
        repository.create(budget2)
        val userBudgets = repository.findByUserId(userId)

        // Then
        assertEquals(2, userBudgets.size)
        assertEquals("Budget 1", userBudgets[0].name)
        assertEquals("Budget 2", userBudgets[1].name)
    }

    @Test
    fun `test update budget`() = runBlocking {
        // Given
        val initialBudget = BudgetDTO(
            id = 0,
            userId = 1,
            name = "Initial Budget",
            description = "Initial Description",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            totalIncome = 1000.0,
            totalExpenses = 500.0,
            createdAt = ""
        )

        // When
        val createdId = repository.create(initialBudget)
        val updatedBudget = initialBudget.copy(
            id = createdId,
            name = "Updated Budget",
            totalIncome = 2000.0
        )
        repository.update(createdId, updatedBudget)
        val retrievedBudget = repository.findById(createdId)

        // Then
        assertNotNull(retrievedBudget)
        assertEquals("Updated Budget", retrievedBudget.name)
        assertEquals(2000.0, retrievedBudget.totalIncome)
    }

    @Test
    fun `test delete budget`() = runBlocking {
        // Given
        val budget = BudgetDTO(
            id = 0,
            userId = 1,
            name = "Test Budget",
            description = "Test Description",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            totalIncome = 1000.0,
            totalExpenses = 500.0,
            createdAt = ""
        )

        // When
        val createdId = repository.create(budget)
        repository.delete(createdId)
        val retrievedBudget = repository.findById(createdId)

        // Then
        assertNull(retrievedBudget)
    }

    @Test
    fun `test find budgets by date range`() = runBlocking {
        // Given
        val userId = 1
        val budget1 = BudgetDTO(
            id = 0,
            userId = userId,
            name = "Q1 Budget",
            description = "Q1",
            startDate = "2024-01-01",
            endDate = "2024-03-31",
            totalIncome = 1000.0,
            totalExpenses = 500.0,
            createdAt = ""
        )
        val budget2 = BudgetDTO(
            id = 0,
            userId = userId,
            name = "Q2 Budget",
            description = "Q2",
            startDate = "2024-04-01",
            endDate = "2024-06-30",
            totalIncome = 2000.0,
            totalExpenses = 1000.0,
            createdAt = ""
        )

        // When
        repository.create(budget1)
        repository.create(budget2)
        val budgets = repository.findByUserIdAndDateRange(
            userId,
            "2024-01-01",
            "2024-03-31"
        )

        // Then
        assertEquals(1, budgets.size)
        assertEquals("Q1 Budget", budgets[0].name)
    }
}