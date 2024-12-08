package com.budgetai.routes.api

import com.budgetai.models.CategoryCreationRequest
import com.budgetai.models.CategoryType
import com.budgetai.models.UpdateCategoryRequest
import com.budgetai.services.CategoryService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes(service: CategoryService) {
    authenticate{
        route("/api/categories") {
            // Create new category
            post {
                try {
                    val request = call.receive<CategoryCreationRequest>()
                    val categoryId = service.createCategory(request)
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
                    val categories = service.getAllCategories()
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

                    val category = service.getCategory(id)
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

                    val category = service.getCategoryByName(name)
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

                    val categories = service.getCategoriesByType(type)
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
                    val existingCategory = service.getCategory(id) ?: throw IllegalArgumentException("Category not found")

                    val updatedCategory = existingCategory.copy(
                        name = request.name, type = request.type, description = request.description
                    )

                    service.updateCategory(id, updatedCategory)
                    call.respond(HttpStatusCode.OK, "Category updated successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, "Error updating category"
                    )
                }
            }

            // update type
            put("/{id}/type") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid category ID")

                    // Create a simple data class for type-only updates
                    data class UpdateCategoryTypeRequest(val type: CategoryType)

                    val request = call.receive<UpdateCategoryTypeRequest>()
                    val existingCategory = service.getCategory(id) ?: throw IllegalArgumentException("Category not found")

                    // Copy existing category but only update the type
                    val updatedCategory = existingCategory.copy(
                        type = request.type,
                        // Preserve existing values
                        name = existingCategory.name,
                        description = existingCategory.description,
                        userId = existingCategory.userId
                    )

                    service.updateCategory(id, updatedCategory)
                    call.respond(HttpStatusCode.OK, "Category type updated successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, "Error updating category type"
                    )
                }
            }

            // Delete category
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid category ID")

                    service.deleteCategory(id)
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
}