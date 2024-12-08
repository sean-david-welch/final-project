package com.budgetai.repositories

import com.budgetai.models.Categories
import com.budgetai.models.CategoryDTO
import com.budgetai.models.CategoryType
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

class CategoryRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: CategoryRepository
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(Categories)
        }

        repository = CategoryRepository(database)
    }

    @After
    fun tearDown() {
        // Clean up tables after each test
        transaction(database) {
            SchemaUtils.drop(Categories)
        }
        // Delete the test database file
        dbFile.delete()
    }

    @Test
    fun `test create and find category by id`() = runBlocking {
        // Given
        val categoryDTO = CategoryDTO(
            id = 0, // ID will be assigned by database
            name = "Groceries", type = CategoryType.FIXED.toString(), description = "Food and household items"
        )

        // When
        val createdId = repository.create(categoryDTO)
        val retrievedCategory = repository.findById(createdId)

        // Then
        assertNotNull(retrievedCategory)
        assertEquals(categoryDTO.name, retrievedCategory.name)
        assertEquals(categoryDTO.type, retrievedCategory.type)
        assertEquals(categoryDTO.description, retrievedCategory.description)
    }

    @Test
    fun `test find category by name`() = runBlocking {
        // Given
        val categoryDTO = CategoryDTO(
            id = 0, name = "Salary", type = CategoryType.FIXED.toString(), description = "Monthly salary"
        )

        // When
        repository.create(categoryDTO)
        val retrievedCategory = repository.findByName("Salary")

        // Then
        assertNotNull(retrievedCategory)
        assertEquals(categoryDTO.name, retrievedCategory.name)
        assertEquals(categoryDTO.type, retrievedCategory.type)
    }

    @Test
    fun `test find all categories`() = runBlocking {
        // Given
        val category1 = CategoryDTO(
            id = 0, name = "Rent", type = CategoryType.FIXED.toString(), description = "Monthly rent"
        )
        val category2 = CategoryDTO(
            id = 0, name = "Bonus", type = CategoryType.FIXED.toString(), description = "Annual bonus"
        )

        // When
        repository.create(category1)
        repository.create(category2)
        val allCategories = repository.findAll()

        // Then
        assertEquals(2, allCategories.size)
        assertEquals(setOf("Rent", "Bonus"), allCategories.map { it.name }.toSet())
    }

    @Test
    fun `test find categories by type`() = runBlocking {
        // Given
        val expenseCategory = CategoryDTO(
            id = 0, name = "Utilities", type = CategoryType.FIXED.toString(), description = "Monthly utilities"
        )
        val incomeCategory = CategoryDTO(
            id = 0, name = "Investments", type = CategoryType.FIXED.toString(), description = "Investment returns"
        )

        // When
        repository.create(expenseCategory)
        repository.create(incomeCategory)
        val expenseCategories = repository.findByType(CategoryType.FIXED.toString())

        // Then
        assertEquals(1, expenseCategories.size)
        assertEquals("Utilities", expenseCategories[0].name)
    }

    @Test
    fun `test update category`() = runBlocking {
        // Given
        val initialCategory = CategoryDTO(
            id = 0, name = "Old Name", type = CategoryType.FIXED.toString(), description = "Old description"
        )

        // When
        val createdId = repository.create(initialCategory)
        val updatedCategory = initialCategory.copy(
            id = createdId, name = "New Name", description = "Updated description"
        )
        repository.update(createdId, updatedCategory)
        val retrievedCategory = repository.findById(createdId)

        // Then
        assertNotNull(retrievedCategory)
        assertEquals("New Name", retrievedCategory.name)
        assertEquals("Updated description", retrievedCategory.description)
    }

    @Test
    fun `test delete category`() = runBlocking {
        // Given
        val category = CategoryDTO(
            id = 0, name = "To Delete", type = CategoryType.FIXED.toString(), description = "Will be deleted"
        )

        // When
        val createdId = repository.create(category)
        repository.delete(createdId)
        val retrievedCategory = repository.findById(createdId)

        // Then
        assertNull(retrievedCategory)
    }
}