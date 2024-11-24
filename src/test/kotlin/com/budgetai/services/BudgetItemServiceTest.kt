package com.budgetai.services

import com.budgetai.models.BudgetItemCreationRequest
import com.budgetai.models.BudgetItemUpdateRequest
import com.budgetai.models.BudgetItems
import com.budgetai.repositories.BudgetItemRepository
import kotlinx.coroutines.runBlocking
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

class BudgetItemServiceTest {
    private lateinit var database: Database
    private lateinit var repository: BudgetItemRepository
    private lateinit var service: BudgetItemService
    private val dbFile = File("test.db")

    private val testBudgetId = 1
    private val testCategoryId = 1

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(BudgetItems)
        }

        repository = BudgetItemRepository(database)
        service = BudgetItemService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(BudgetItems)
        }
        dbFile.delete()
    }

    @Test
    fun `createBudgetItem should create item with valid request`() = runBlocking {
        // Given
        val request = BudgetItemCreationRequest(
            budgetId = testBudgetId, categoryId = testCategoryId, name = "Groceries", amount = 500.0
        )

        // When
        val itemId = service.createBudgetItem(request)
        val createdItem = service.getBudgetItem(itemId)

        // Then
        assertEquals(request.name, createdItem?.name)
        assertEquals(request.amount, createdItem?.amount)
        assertEquals(request.budgetId, createdItem?.budgetId)
        assertEquals(request.categoryId, createdItem?.categoryId)
    }

    @Test
    fun `createBudgetItem should throw exception for negative amount`(): Unit = runBlocking {
        // Given
        val request = BudgetItemCreationRequest(
            budgetId = testBudgetId, categoryId = testCategoryId, name = "Groceries", amount = -100.0
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createBudgetItem(request)
        }
    }

    @Test
    fun `createBulkBudgetItems should create multiple items`() = runBlocking {
        // Given
        val requests = listOf(
            BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Groceries", amount = 500.0
            ), BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Utilities", amount = 300.0
            )
        )

        // When
        val itemIds = service.createBulkBudgetItems(requests)
        val createdItems = itemIds.mapNotNull { service.getBudgetItem(it) }

        // Then
        assertEquals(2, createdItems.size)
        assertEquals(requests[0].name, createdItems[0].name)
        assertEquals(requests[1].name, createdItems[1].name)
    }

    @Test
    fun `updateBudgetItem should update item details correctly`() = runBlocking {
        // Given
        val createRequest = BudgetItemCreationRequest(
            budgetId = testBudgetId, categoryId = testCategoryId, name = "Original Name", amount = 500.0
        )
        val itemId = service.createBudgetItem(createRequest)

        val updateRequest = BudgetItemUpdateRequest(
            name = "Updated Name", amount = 600.0
        )

        // When
        service.updateBudgetItem(itemId, updateRequest)
        val updatedItem = service.getBudgetItem(itemId)

        // Then
        assertEquals(updateRequest.name, updatedItem?.name)
        assertEquals(updateRequest.amount, updatedItem?.amount)
    }

    @Test
    fun `updateBudgetItemAmount should update amount correctly`() = runBlocking {
        // Given
        val createRequest = BudgetItemCreationRequest(
            budgetId = testBudgetId, categoryId = testCategoryId, name = "Test Item", amount = 500.0
        )
        val itemId = service.createBudgetItem(createRequest)

        // When
        service.updateBudgetItemAmount(itemId, 600.0)
        val updatedItem = service.getBudgetItem(itemId)

        // Then
        assertEquals(600.0, updatedItem?.amount)
    }

    @Test
    fun `getBudgetItems should return all items for budget`() = runBlocking {
        // Given
        val requests = listOf(
            BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 1", amount = 100.0
            ), BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 2", amount = 200.0
            )
        )
        service.createBulkBudgetItems(requests)

        // When
        val items = service.getBudgetItems(testBudgetId)

        // Then
        assertEquals(2, items.size)
        assertEquals(300.0, service.getBudgetTotalAmount(testBudgetId))
    }

    @Test
    fun `getCategoryItems should return all items for category`() = runBlocking {
        // Given
        val requests = listOf(
            BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 1", amount = 100.0
            ), BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId + 1, name = "Item 2", amount = 200.0
            )
        )
        service.createBulkBudgetItems(requests)

        // When
        val items = service.getCategoryItems(testCategoryId)

        // Then
        assertEquals(1, items.size)
        assertEquals(100.0, service.getCategoryTotalAmount(testBudgetId, testCategoryId))
    }

    @Test
    fun `deleteBudgetItem should remove item`() = runBlocking {
        // Given
        val request = BudgetItemCreationRequest(
            budgetId = testBudgetId, categoryId = testCategoryId, name = "Test Item", amount = 500.0
        )
        val itemId = service.createBudgetItem(request)

        // When
        service.deleteBudgetItem(itemId)
        val deletedItem = service.getBudgetItem(itemId)

        // Then
        assertNull(deletedItem)
    }

    @Test
    fun `deleteBudgetItems should remove all items for budget`() = runBlocking {
        // Given
        val requests = listOf(
            BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 1", amount = 100.0
            ), BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 2", amount = 200.0
            )
        )
        service.createBulkBudgetItems(requests)

        // When
        service.deleteBudgetItems(testBudgetId)
        val remainingItems = service.getBudgetItems(testBudgetId)

        // Then
        assertTrue(remainingItems.isEmpty())
    }

    @Test
    fun `deleteCategoryItems should remove all items for category`() = runBlocking {
        // Given
        val requests = listOf(
            BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 1", amount = 100.0
            ), BudgetItemCreationRequest(
                budgetId = testBudgetId, categoryId = testCategoryId, name = "Item 2", amount = 200.0
            )
        )
        service.createBulkBudgetItems(requests)

        // When
        service.deleteCategoryItems(testCategoryId)
        val remainingItems = service.getCategoryItems(testCategoryId)

        // Then
        assertTrue(remainingItems.isEmpty())
    }
}