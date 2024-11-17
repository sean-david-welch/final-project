package com.budgetai.routes

import com.budgetai.models.BudgetDTO
import com.budgetai.repositories.BudgetRepository
import com.budgetai.services.BudgetService
import com.budgetai.services.BudgetService.BudgetCreationRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import java.math.BigDecimal

fun Route.budgetRoutes(database: Database) {
    // Initialize repositories and services
    val budgetRepository = BudgetRepository(database)
    val budgetService = BudgetService(budgetRepository)

    // Data classes for requests
    @Serializable
    data class UpdateBudgetRequest(
        val name: String,
        val description: String?,
        val startDate: String?,
        val endDate: String?
    )

    @Serializable
    data class UpdateBudgetTotalsRequest(
        val totalIncome: Double,
        val totalExpenses: Double
    )

    route("/budgets") {
        // Create new budget
        post {
            try {
                val request = call.receive<BudgetCreationRequest>()
                val budgetId = budgetService.createBudget(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to budgetId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating budget")
            }
        }

        // Get budget by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                val budget = budgetService.getBudget(id)
                if (budget != null) {
                    call.respond(budget)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Budget not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budget")
            }
        }

        // Get all budgets for a user
        get("/user/{userId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")

                val budgets = budgetService.getUserBudgets(userId)
                call.respond(budgets)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budgets")
            }
        }

        // Get budgets for a user within a date range
        get("/user/{userId}/date-range") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")
                val startDate = call.parameters["startDate"]?.let { LocalDate.parse(it) }
                    ?: throw IllegalArgumentException("Start date is required")
                val endDate = call.parameters["endDate"]?.let { LocalDate.parse(it) }
                    ?: throw IllegalArgumentException("End date is required")

                val budgets = budgetService.getUserBudgetsInDateRange(userId, startDate, endDate)
                call.respond(budgets)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving budgets")
            }
        }

        // Update budget
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                val request = call.receive<UpdateBudgetRequest>()
                val existingBudget = budgetService.getBudget(id)
                    ?: throw IllegalArgumentException("Budget not found")

                val updatedBudget = existingBudget.copy(
                    name = request.name,
                    description = request.description,
                    startDate = request.startDate,
                    endDate = request.endDate
                )

                budgetService.updateBudget(id, updatedBudget)
                call.respond(HttpStatusCode.OK, "Budget updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating budget")
            }
        }

        // Update budget totals
        put("/{id}/totals") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                val request = call.receive<UpdateBudgetTotalsRequest>()
                budgetService.updateBudgetTotals(
                    id,
                    BigDecimal.valueOf(request.totalIncome),
                    BigDecimal.valueOf(request.totalExpenses)
                )
                call.respond(HttpStatusCode.OK, "Budget totals updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating budget totals")
            }
        }

        // Delete budget
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget ID")

                budgetService.deleteBudget(id)
                call.respond(HttpStatusCode.OK, "Budget deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget")
            }
        }

        // Delete all budgets for a user
        delete("/user/{userId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid user ID")

                budgetService.deleteUserBudgets(userId)
                call.respond(HttpStatusCode.OK, "User budgets deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting user budgets")
            }
        }
    }
}