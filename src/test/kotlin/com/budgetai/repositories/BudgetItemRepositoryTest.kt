package com.budgetai.repositories

import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.BudgetItems
import com.budgetai.models.Budgets
import com.budgetai.models.Categories
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BudgetItemRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: BudgetItemRepository
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
            SchemaUtils.create(Categories, Budgets, BudgetItems)
        }

        repository = BudgetItemRepository(database)
    }

    @After
    fun tearDown() {
        // Clean up tables after each test
        transaction(database) {
            SchemaUtils.drop(BudgetItems, Budgets, Categories)
        }
        dbFile.delete()
    }

    private fun createSampleBudgetItem(
        budgetId: Int = 1,
        categoryId: Int = 1,
        name: String = "Test Item",
        amount: Double = 100.0
    ): BudgetItemDTO {
        return BudgetItemDTO(
            id = 0,
            budgetId = budgetId,
            categoryId = categoryId,
            name = name,
            amount = amount,
            createdAt = ""
        )
    }

    @Test
    fun `test create and find budget item by id`() = runBlocking {
        // Given
        val budgetItem = createSampleBudgetItem()

        // When
        val createdId = repository.create(budgetItem)
        val retrievedItem = repository.findById(createdId)

        // Then
        assertNotNull(retrievedItem)
        assertEquals(budgetItem.name, retrievedItem.name)
        assertEquals(budgetItem.amount, retrievedItem.amount)
        assertEquals(budgetItem.budgetId, retrievedItem.budgetId)
        assertEquals(budgetItem.categoryId, retrievedItem.categoryId)
    }

    @Test
    fun `test find items by budget id`() = runBlocking {
        // Given
        val budgetId = 1
        val item1 = createSampleBudgetItem(budgetId = budgetId, name = "Item 1")
        val item2 = createSampleBudgetItem(budgetId = budgetId, name = "Item 2")

        // When
        repository.create(item1)
        repository.create(item2)
        val budgetItems = repository.findByBudgetId(budgetId)

        // Then
        assertEquals(2, budgetItems.size)
        assertEquals(setOf("Item 1", "Item 2"), budgetItems.map { it.name }.toSet())
    }

    @Test
    fun `test find items by category id`() = runBlocking {
        // Given
        val categoryId = 1
        val item1 = createSampleBudgetItem(categoryId = categoryId, name = "Category Item 1")
        val item2 = createSampleBudgetItem(categoryId = categoryId, name = "Category Item 2")

        // When
        repository.create(item1)
        repository.create(item2)
        val categoryItems = repository.findByCategoryId(categoryId)

        // Then
        assertEquals(2, categoryItems.size)
        assertEquals(setOf("Category Item 1", "Category Item 2"), categoryItems.map { it.name }.toSet())
    }

    @Test
    fun `test get total amount by budget id`() = runBlocking {
        // Given
        val budgetId = 1
        val item1 = createSampleBudgetItem(budgetId = budgetId, amount = 100.0)
        val item2 = createSampleBudgetItem(budgetId = budgetId, amount = 200.0)

        // When
        repository.create(item1)
        repository.create(item2)
        val totalAmount = repository.getTotalAmountByBudgetId(budgetId)

        // Then
        assertEquals(300.0, totalAmount)
    }

    @Test
    fun `test get total amount by category within budget`() = runBlocking {
        // Given
        val budgetId = 1
        val categoryId = 1
        val item1 = createSampleBudgetItem(budgetId = budgetId, categoryId = categoryId, amount = 150.0)
        val item2 = createSampleBudgetItem(budgetId = budgetId, categoryId = categoryId, amount = 250.0)

        // When
        repository.create(item1)
        repository.create(item2)
        val totalAmount = repository.getTotalAmountByCategory(budgetId, categoryId)

        // Then
        assertEquals(400.0, totalAmount)
    }

    @Test
    fun `test update budget item`() = runBlocking {
        // Given
        val initialItem = createSampleBudgetItem()

        // When
        val createdId = repository.create(initialItem)
        val updatedItem = initialItem.copy(
            id = createdId,
            name = "Updated Item",
            amount = 200.0
        )
        repository.update(createdId, updatedItem)
        val retrievedItem = repository.findById(createdId)

        // Then
        assertNotNull(retrievedItem)
        assertEquals("Updated Item", retrievedItem.name)
        assertEquals(200.0, retrievedItem.amount)
    }

    @Test
    fun `test update amount only`() = runBlocking {
        // Given
        val item = createSampleBudgetItem(amount = 100.0)

        // When
        val createdId = repository.create(item)
        repository.updateAmount(createdId, 150.0)
        val retrievedItem = repository.findById(createdId)

        // Then
        assertNotNull(retrievedItem)
        assertEquals(150.0, retrievedItem.amount)
        assertEquals(item.name, retrievedItem.name) // Other fields should remain unchanged
    }

    @Test
    fun `test delete operations`() = runBlocking {
        // Given
        val budgetId = 1
        val categoryId = 1
        val item1 = createSampleBudgetItem(budgetId = budgetId, categoryId = categoryId)
        val item2 = createSampleBudgetItem(budgetId = budgetId, categoryId = categoryId)

        // When - Test single item deletion
        val id1 = repository.create(item1)
        val id2 = repository.create(item2)
        repository.delete(id1)

        // Then
        assertNull(repository.findById(id1))
        assertNotNull(repository.findById(id2))

        // When - Test deletion by budget ID
        repository.deleteByBudgetId(budgetId)
        assertEquals(0, repository.findByBudgetId(budgetId).size)

        // When - Test deletion by category ID
        val item3 = createSampleBudgetItem(categoryId = categoryId)
        repository.create(item3)
        repository.deleteByCategoryId(categoryId)
        assertEquals(0, repository.findByCategoryId(categoryId).size)
    }

    @Test
    fun `test batch create budget items`() = runBlocking {
        // Given
        val items = listOf(
            createSampleBudgetItem(name = "Batch Item 1", amount = 100.0),
            createSampleBudgetItem(name = "Batch Item 2", amount = 200.0),
            createSampleBudgetItem(name = "Batch Item 3", amount = 300.0)
        )

        // When
        val createdIds = repository.createBatch(items)
        val retrievedItems = createdIds.mapNotNull { repository.findById(it) }

        // Then
        assertEquals(3, retrievedItems.size)
        assertEquals(items.map { it.name }.toSet(), retrievedItems.map { it.name }.toSet())
        assertEquals(600.0, repository.getTotalAmountByBudgetId(1))
    }
}