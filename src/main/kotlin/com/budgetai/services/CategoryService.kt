package com.budgetai.services

import com.budgetai.models.CategoryDTO
import com.budgetai.models.CategoryType
import com.budgetai.repositories.CategoryRepository

class CategoryService(private val repository: CategoryRepository) {

    data class CategoryCreationRequest(
        val name: String,
        val type: CategoryType,
        val description: String? = null
    )

    suspend fun createCategory(request: CategoryCreationRequest): Int {
        // Check if name already exists
        repository.findByName(request.name)?.let {
            throw IllegalArgumentException("Category name already exists")
        }

        // Create category
        val categoryDTO = CategoryDTO(
            name = request.name,
            type = request.type,
            description = request.description
        )

        return repository.create(categoryDTO)
    }

    suspend fun getCategory(id: Int): CategoryDTO? {
        return repository.findById(id)
    }

    suspend fun getCategoryByName(name: String): CategoryDTO? {
        return repository.findByName(name)
    }

    suspend fun getAllCategories(): List<CategoryDTO> {
        return repository.findAll()
    }

    suspend fun getCategoriesByType(type: CategoryType): List<CategoryDTO> {
        return repository.findByType(type)
    }

    suspend fun updateCategory(id: Int, category: CategoryDTO) {
        // Validate if category exists
        val existingCategory = repository.findById(id)
            ?: throw IllegalArgumentException("Category not found")

        // Check if new name conflicts with existing category (excluding current category)
        if (category.name != existingCategory.name) {
            repository.findByName(category.name)?.let {
                throw IllegalArgumentException("Category name already in use")
            }
        }

        repository.update(id, category)
    }

    suspend fun deleteCategory(id: Int) {
        // Optionally, verify category exists before deletion
        repository.findById(id) ?: throw IllegalArgumentException("Category not found")
        repository.delete(id)
    }
}