package com.budgetai.routes

import com.budgetai.models.BudgetItemDTO
import com.budgetai.repositories.BudgetItemRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

fun Route.budgetItemRoutes(database: Database) {
    val budgetItemRepository = BudgetItemRepository(database)

    @Serializable
    data class CreateBudgetItemRequest(
        val budgetId: Int, val categoryId: Int, val name: String, val amount: Double
    )

    @Serializable
    data class UpdateBudgetItemRequest(
        val budgetId: Int, val categoryId: Int, val name: String, val amount: Double
    )

    @Serializable
    data class UpdateAmountRequest(
        val amount: Double
    )

    @Serializable
    data class BatchCreateRequest(
        val items: List<CreateBudgetItemRequest>
    )

    route("/budget-items") {
        // Create new budget item
        post {
            try {
                val request = call.receive<CreateBudgetItemRequest>()
                val budgetItem = BudgetItemDTO(
                    id = 0, // Will be set by the database
                    budgetId = request.budgetId,
                    categoryId = request.categoryId,
                    name = request.name,
                    amount = request.amount,
                    createdAt = "" // Will be set by the database
                )
                val id = budgetItemRepository.create(budgetItem)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating budget item")
            }
        }

        // Batch create budget items
        post("/batch") {
            try {
                val request = call.receive<BatchCreateRequest>()
                val budgetItems = request.items.map { item ->
                    BudgetItemDTO(
                        id = 0,
                        budgetId = item.budgetId,
                        categoryId = item.categoryId,
                        name = item.name,
                        amount = item.amount,
                        createdAt = ""
                    )
                }
                val ids = budgetItemRepository.createBatch(budgetItems)
                call.respond(HttpStatusCode.Created, mapOf("ids" to ids))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating budget items")
            }
        }

        // Get budget item by ID
        get("/{id}") {
            try {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget item ID")
                val budgetItem = budgetItemRepository.findById(id)
                if (budgetItem != null) {
                    call.respond(budgetItem)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Budget item not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budget item")
            }
        }

        // Get all budget items for a budget
        get("/budget/{budgetId}") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")
                val items = budgetItemRepository.findByBudgetId(budgetId)
                call.respond(items)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budget items")
            }
        }

        // Get all budget items for a category
        get("/category/{categoryId}") {
            try {
                val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid category ID")
                val items = budgetItemRepository.findByCategoryId(categoryId)
                call.respond(items)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budget items")
            }
        }

        // Get total amount for a budget
        get("/budget/{budgetId}/total") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")
                val total = budgetItemRepository.getTotalAmountByBudgetId(budgetId)
                call.respond(mapOf("total" to total))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error calculating total")
            }
        }

        // Get total amount for a category within a budget
        get("/budget/{budgetId}/category/{categoryId}/total") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")
                val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid category ID")
                val total = budgetItemRepository.getTotalAmountByCategory(budgetId, categoryId)
                call.respond(mapOf("total" to total))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error calculating total")
            }
        }

        // Update budget item
        put("/{id}") {
            try {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget item ID")
                val request = call.receive<UpdateBudgetItemRequest>()

                val budgetItem = BudgetItemDTO(
                    id = id,
                    budgetId = request.budgetId,
                    categoryId = request.categoryId,
                    name = request.name,
                    amount = request.amount,
                    createdAt = "" // Will be preserved by the update method
                )

                budgetItemRepository.update(id, budgetItem)
                call.respond(HttpStatusCode.OK, "Budget item updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating budget item")
            }
        }

        // Update budget item amount only
        put("/{id}/amount") {
            try {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget item ID")
                val request = call.receive<UpdateAmountRequest>()

                budgetItemRepository.updateAmount(id, request.amount)
                call.respond(HttpStatusCode.OK, "Budget item amount updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating budget item amount")
            }
        }

        // Delete budget item
        delete("/{id}") {
            try {
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget item ID")
                budgetItemRepository.delete(id)
                call.respond(HttpStatusCode.OK, "Budget item deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget item")
            }
        }

        // Delete all budget items for a budget
        delete("/budget/{budgetId}") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")
                budgetItemRepository.deleteByBudgetId(budgetId)
                call.respond(HttpStatusCode.OK, "Budget items deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget items")
            }
        }

        // Delete all budget items for a category
        delete("/category/{categoryId}") {
            try {
                val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid category ID")
                budgetItemRepository.deleteByCategoryId(categoryId)
                call.respond(HttpStatusCode.OK, "Budget items deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget items")
            }
        }
    }
}