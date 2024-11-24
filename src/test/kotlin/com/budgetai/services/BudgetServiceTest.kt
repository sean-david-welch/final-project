package com.budgetai.services

import com.budgetai.models.BudgetDTO
import com.budgetai.repositories.BudgetRepository
import com.budgetai.models.Budgets
import com.budgetai.models.Users
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking

class BudgetServiceTest {
    private lateinit var database: Database
    private lateinit var repository: BudgetRepository
    private lateinit var service: BudgetService
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
        service = BudgetService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Budgets, Users)
        }
        dbFile.delete()
    }

    @Test
    fun `createBudget should create budget with valid request`() = runBlocking {
        // Given
        val request = BudgetService.BudgetCreationRequest(
            userId = 1,
            name = "Test Budget",
            description = "Test Description",
            startDate = LocalDate.parse("2024-01-01"),
            endDate = LocalDate.parse("2024-12-31")
        )

        // When
        val budgetId = service.createBudget(request)
        val createdBudget = service.getBudget(budgetId)

        // Then
        assertEquals(request.name, createdBudget?.name)
        assertEquals(request.description, createdBudget?.description)
        assertEquals(request.startDate.toString(), createdBudget?.startDate)
        assertEquals(request.endDate.toString(), createdBudget?.endDate)
    }

    @Test
    fun `createBudget should throw exception when end date is before start date`() = runBlocking {
        // Given
        val request = BudgetService.BudgetCreationRequest(
            userId = 1,
            name = "Test Budget",
            startDate = LocalDate.parse("2024-12-31"),
            endDate = LocalDate.parse("2024-01-01")
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createBudget(request)
        }
    }

    @Test
    fun `updateBudgetTotals should update when values are valid`() = runBlocking {
        // Given
        val request = BudgetService.BudgetCreationRequest(
            userId = 1,
            name = "Test Budget"
        )
        val budgetId = service.createBudget(request)
        val totalIncome = BigDecimal("1000.00")
        val totalExpenses = BigDecimal("500.00")

        // When
        service.updateBudgetTotals(budgetId, totalIncome, totalExpenses)
        val updatedBudget = service.getBudget(budgetId)

        // Then
        assertEquals(totalIncome.toDouble(), updatedBudget?.totalIncome)
        assertEquals(totalExpenses.toDouble(), updatedBudget?.totalExpenses)
    }

    @Test
    fun `updateBudgetTotals should throw exception for negative income`() = runBlocking {
        // Given
        val request = BudgetService.BudgetCreationRequest(
            userId = 1,
            name = "Test Budget"
        )
        val budgetId = service.createBudget(request)
        val totalIncome = BigDecimal("-1000.00")
        val totalExpenses = BigDecimal("500.00")

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.updateBudgetTotals(budgetId, totalIncome, totalExpenses)
        }
    }

    @Test
    fun `getUserBudgetsInDateRange should return filtered budgets`() = runBlocking {
        // Given
        val userId = 1
        val startDate = LocalDate.parse("2024-01-01")
        val endDate = LocalDate.parse("2024-12-31")

        // Create two budgets, one in range and one out of range
        service.createBudget(
            BudgetService.BudgetCreationRequest(
                userId = userId,
                name = "In Range",
                startDate = startDate,
                endDate = endDate
            )
        )
        service.createBudget(
            BudgetService.BudgetCreationRequest(
                userId = userId,
                name = "Out of Range",
                startDate = LocalDate.parse("2025-01-01"),
                endDate = LocalDate.parse("2025-12-31")
            )
        )

        // When
        val result = service.getUserBudgetsInDateRange(userId, startDate, endDate)

        // Then
        assertEquals(1, result.size)
        assertEquals("In Range", result[0].name)
    }
}