package com.budgetai.services

import com.budgetai.models.CategoryDTO
import com.budgetai.models.CategoryType
import com.budgetai.repositories.CategoryRepository

class CategoryService(private val repository: CategoryRepository) {

    // Data Models
    // Request model for creating a new category
    data class CategoryCreationRequest(
        val name: String,
        val type: CategoryType,
        val description: String? = null
    )

    // Helper Methods
    // Verifies a category exists and returns it or throws exception
    private suspend fun validateCategoryExists(id: Int): CategoryDTO {
        return repository.findById(id) ?: throw IllegalArgumentException("Category not found")
    }

    // Checks if a category name is already in use, excluding a specific category ID
    private suspend fun validateNameUnique(name: String, excludeId: Int? = null) {
        repository.findByName(name)?.let { existing ->
            if (excludeId == null || existing.id != excludeId) {
                throw IllegalArgumentException("Category name already in use")
            }
        }
    }

    // Read Methods
    // Retrieves a single category by ID
    suspend fun getCategory(id: Int): CategoryDTO? {
        return repository.findById(id)
    }

    // Retrieves a category by its name
    suspend fun getCategoryByName(name: String): CategoryDTO? {
        return repository.findByName(name)
    }

    // Retrieves all categories
    suspend fun getAllCategories(): List<CategoryDTO> {
        return repository.findAll()
    }

    // Retrieves categories of a specific type
    suspend fun getCategoriesByType(type: CategoryType): List<CategoryDTO> {
        return repository.findByType(type)
    }

    // Write Methods
    // Creates a new category and returns its ID
    suspend fun createCategory(request: CategoryCreationRequest): Int {
        validateNameUnique(request.name)

        val categoryDTO = CategoryDTO(
            name = request.name,
            type = request.type,
            description = request.description
        )

        return repository.create(categoryDTO)
    }

    // Updates an existing category's details
    suspend fun updateCategory(id: Int, category: CategoryDTO) {
        val existingCategory = validateCategoryExists(id)

        if (category.name != existingCategory.name) {
            validateNameUnique(category.name, id)
        }

        repository.update(id, category)
    }

    // Deletes a category by ID
    suspend fun deleteCategory(id: Int) {
        validateCategoryExists(id)
        repository.delete(id)
    }
}