package com.budgetai.routes.api

import com.budgetai.models.*
import com.budgetai.services.CategoryService
import com.budgetai.templates.components.ResponseComponents
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.categoryRoutes(service: CategoryService) {
    val logger = LoggerFactory.getLogger("CategoryRoutes")
    authenticate {
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

                    // Log category lookup
                    val existingCategory = service.getCategory(id) ?: throw IllegalArgumentException("Category not found")

                    // Handle both JSON and form-encoded requests
                    val requestBody = when (call.request.contentType()) {
                        ContentType.Application.Json -> call.receive<UpdateCategoryTypeRequest>()
                        ContentType.Application.FormUrlEncoded -> {
                            val parameters = call.receiveParameters()
                            UpdateCategoryTypeRequest(
                                type = parameters["type"] ?: throw IllegalArgumentException("Type is required")
                            )
                        }
                        else -> throw IllegalArgumentException("Unsupported content type")
                    }

                    // Prepare and update category
                    val updatedCategory = existingCategory.copy(
                        id = id,
                        userId = existingCategory.userId,
                        createdAt = existingCategory.createdAt,
                        type = requestBody.type,
                        name = existingCategory.name,
                        description = existingCategory.description,
                    )

                    service.updateCategory(id, updatedCategory)
                    logger.info("Successfully updated category ${id} type to '${requestBody.type}'")

                    // Send appropriate response based on content type
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Category type updated successfully"))
                        }
                        else -> {
                            call.respondText(
                                ResponseComponents.success("Category type updated successfully"),
                                ContentType.Text.Html,
                                HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    logger.warn("Bad request while updating category type", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
                        }
                        else -> {
                            call.respondText(
                                ResponseComponents.error(e.message ?: "Invalid request"),
                                ContentType.Text.Html,
                                HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Unexpected error while updating category type", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error updating category type"))
                        }
                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error updating category type"),
                                ContentType.Text.Html,
                                HttpStatusCode.OK
                            )
                        }
                    }
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