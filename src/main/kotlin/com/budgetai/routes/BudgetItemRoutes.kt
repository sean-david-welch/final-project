package com.budgetai.routes

import com.budgetai.repositories.BudgetItemRepository
import com.budgetai.services.BudgetItemService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

fun Route.budgetItemServiceRoutes(database: Database) {
    val budgetItemRepository = BudgetItemRepository(database)
    val budgetItemService = BudgetItemService(budgetItemRepository)

    @Serializable
    data class UpdateAmountRequest(
        val amount: Double
    )

    route("/budget-items") {
        // Create new budget item
        post {
            try {
                val request = call.receive<BudgetItemService.BudgetItemCreationRequest>()
                val itemId = budgetItemService.createBudgetItem(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to itemId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating budget item")
            }
        }

        // Bulk create budget items
        post("/bulk") {
            try {
                val requests = call.receive<List<BudgetItemService.BudgetItemCreationRequest>>()
                val itemIds = budgetItemService.createBulkBudgetItems(requests)
                call.respond(HttpStatusCode.Created, mapOf("ids" to itemIds))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating budget items")
            }
        }

        // Get budget item by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")

                val item = budgetItemService.getBudgetItem(id)
                if (item != null) {
                    call.respond(item)
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
                val budgetId = call.parameters["budgetId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                val items = budgetItemService.getBudgetItems(budgetId)
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

                val items = budgetItemService.getCategoryItems(categoryId)
                call.respond(items)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving category items")
            }
        }

        // Get total amount for a budget
        get("/budget/{budgetId}/total") {
            try {
                val budgetId = call.parameters["budgetId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                val total = budgetItemService.getBudgetTotalAmount(budgetId)
                call.respond(mapOf("total" to total))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error calculating total amount")
            }
        }

        // Get total amount for a category within a budget
        get("/budget/{budgetId}/category/{categoryId}/total") {
            try {
                val budgetId = call.parameters["budgetId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")
                val categoryId = call.parameters["categoryId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid category ID")

                val total = budgetItemService.getCategoryTotalAmount(budgetId, categoryId)
                call.respond(mapOf("total" to total))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error calculating category total")
            }
        }

        // Update budget item
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")
                val request = call.receive<BudgetItemService.BudgetItemUpdateRequest>()

                budgetItemService.updateBudgetItem(id, request)
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
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")
                val request = call.receive<UpdateAmountRequest>()

                budgetItemService.updateBudgetItemAmount(id, request.amount)
                call.respond(HttpStatusCode.OK, "Amount updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating amount")
            }
        }

        // Delete budget item
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")

                budgetItemService.deleteBudgetItem(id)
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
                val budgetId = call.parameters["budgetId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                budgetItemService.deleteBudgetItems(budgetId)
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

                budgetItemService.deleteCategoryItems(categoryId)
                call.respond(HttpStatusCode.OK, "Category items deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting category items")
            }
        }
    }
}