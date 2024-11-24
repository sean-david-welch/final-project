package com.budgetai.services

import com.budgetai.models.Categories
import com.budgetai.models.CategoryDTO
import com.budgetai.models.CategoryType
import com.budgetai.repositories.CategoryRepository
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

class CategoryServiceTest {
    private lateinit var database: Database
    private lateinit var repository: CategoryRepository
    private lateinit var service: CategoryService
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
        service = CategoryService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Categories)
        }
        dbFile.delete()
    }

    @Test
    fun `createCategory should create category with valid request`() = runBlocking {
        // Given
        val request = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE, description = "Food and household items"
        )

        // When
        val categoryId = service.createCategory(request)
        val createdCategory = service.getCategory(categoryId)

        // Then
        assertEquals(request.name, createdCategory?.name)
        assertEquals(request.type, createdCategory?.type)
        assertEquals(request.description, createdCategory?.description)
    }

    @Test
    fun `createCategory should throw exception when name already exists`() = runBlocking {
        // Given
        val request = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE
        )
        service.createCategory(request)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createCategory(request)
        }
    }

    @Test
    fun `updateCategory should update when values are valid`() = runBlocking {
        // Given
        val request = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE
        )
        val categoryId = service.createCategory(request)
        val updatedCategory = CategoryDTO(
            id = categoryId, name = "Food", type = CategoryType.EXPENSE, description = "Updated description"
        )

        // When
        service.updateCategory(categoryId, updatedCategory)
        val result = service.getCategory(categoryId)

        // Then
        assertEquals(updatedCategory.name, result?.name)
        assertEquals(updatedCategory.description, result?.description)
    }

    @Test
    fun `updateCategory should throw exception when updating to existing name`() = runBlocking {
        // Given
        val firstCategory = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE
        )
        val secondCategory = CategoryService.CategoryCreationRequest(
            name = "Entertainment", type = CategoryType.EXPENSE
        )
        service.createCategory(firstCategory)
        val secondId = service.createCategory(secondCategory)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.updateCategory(
                secondId, CategoryDTO(id = secondId, name = "Groceries", type = CategoryType.EXPENSE)
            )
        }
    }

    @Test
    fun `deleteCategory should remove existing category`() = runBlocking {
        // Given
        val request = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE
        )
        val categoryId = service.createCategory(request)

        // When
        service.deleteCategory(categoryId)
        val deletedCategory = service.getCategory(categoryId)

        // Then
        assertNull(deletedCategory)
    }

    @Test
    fun `getCategoriesByType should return filtered categories`() = runBlocking {
        // Given
        service.createCategory(
            CategoryService.CategoryCreationRequest(
                name = "Salary", type = CategoryType.INCOME
            )
        )
        service.createCategory(
            CategoryService.CategoryCreationRequest(
                name = "Groceries", type = CategoryType.EXPENSE
            )
        )

        // When
        val incomeCategories = service.getCategoriesByType(CategoryType.INCOME)
        val expenseCategories = service.getCategoriesByType(CategoryType.EXPENSE)

        // Then
        assertEquals(1, incomeCategories.size)
        assertEquals("Salary", incomeCategories[0].name)
        assertEquals(1, expenseCategories.size)
        assertEquals("Groceries", expenseCategories[0].name)
    }

    @Test
    fun `getAllCategories should return all categories`() = runBlocking {
        // Given
        service.createCategory(
            CategoryService.CategoryCreationRequest(
                name = "Salary", type = CategoryType.INCOME
            )
        )
        service.createCategory(
            CategoryService.CategoryCreationRequest(
                name = "Groceries", type = CategoryType.EXPENSE
            )
        )

        // When
        val allCategories = service.getAllCategories()

        // Then
        assertEquals(2, allCategories.size)
    }

    @Test
    fun `getCategoryByName should return correct category`() = runBlocking {
        // Given
        val request = CategoryService.CategoryCreationRequest(
            name = "Groceries", type = CategoryType.EXPENSE
        )
        service.createCategory(request)

        // When
        val category = service.getCategoryByName("Groceries")

        // Then
        assertEquals(request.name, category?.name)
        assertEquals(request.type, category?.type)
    }
}