package com.budgetai.routes

import com.budgetai.models.CategoryCreationRequest
import com.budgetai.models.CategoryType
import com.budgetai.models.UpdateCategoryRequest
import com.budgetai.repositories.CategoryRepository
import com.budgetai.services.CategoryService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.categoryRoutes(database: Database) {
    // Initialize repositories and services
    val categoryRepository = CategoryRepository(database)
    val categoryService = CategoryService(categoryRepository)

    route("/categories") {
        // Create new category
        post {
            try {
                val request = call.receive<CategoryCreationRequest>()
                val categoryId = categoryService.createCategory(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to categoryId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating category")
            }
        }

        // Get all categories
        get {
            try {
                val categories = categoryService.getAllCategories()
                call.respond(categories)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error retrieving categories"
                )
            }
        }

        // Get category by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid category ID")

                val category = categoryService.getCategory(id)
                if (category != null) {
                    call.respond(category)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Category not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error retrieving category"
                )
            }
        }

        // Get category by name
        get("/name/{name}") {
            try {
                val name = call.parameters["name"] ?: throw IllegalArgumentException("Name is required")

                val category = categoryService.getCategoryByName(name)
                if (category != null) {
                    call.respond(category)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Category not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error retrieving category"
                )
            }
        }

        // Get categories by type
        get("/type/{type}") {
            try {
                val typeStr = call.parameters["type"] ?: throw IllegalArgumentException("Type is required")

                val type = try {
                    CategoryType.valueOf(typeStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid category type")
                }

                val categories = categoryService.getCategoriesByType(type)
                call.respond(categories)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error retrieving categories"
                )
            }
        }

        // Update category
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid category ID")

                val request = call.receive<UpdateCategoryRequest>()
                val existingCategory = categoryService.getCategory(id) ?: throw IllegalArgumentException("Category not found")

                val updatedCategory = existingCategory.copy(
                    name = request.name, type = request.type, description = request.description
                )

                categoryService.updateCategory(id, updatedCategory)
                call.respond(HttpStatusCode.OK, "Category updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error updating category"
                )
            }
        }

        // Delete category
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid category ID")

                categoryService.deleteCategory(id)
                call.respond(HttpStatusCode.OK, "Category deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, "Error deleting category"
                )
            }
        }
    }
}