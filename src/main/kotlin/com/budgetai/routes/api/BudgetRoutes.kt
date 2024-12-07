package com.budgetai.routes.api

import com.budgetai.models.BudgetCreationRequest
import com.budgetai.models.UpdateBudgetRequest
import com.budgetai.models.UpdateBudgetTotalsRequest
import com.budgetai.services.BudgetItemService
import com.budgetai.services.BudgetService
import com.budgetai.templates.components.ResponseComponents
import com.budgetai.utils.BudgetParser
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.math.BigDecimal

fun Route.budgetRoutes(service: BudgetService, budgetItemService: BudgetItemService) {
    val logger = LoggerFactory.getLogger("Budget routes")

    authenticate {
        route("/api/budgets") {
            // Create new budget
            post {
                try {
                    logger.info("Received budget creation request with content type: ${call.request.contentType()}")

                    val budgetId = when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            val request = call.receive<BudgetCreationRequest>()
                            logger.info("Processing JSON request: userId=${request.userId}, name=${request.name}")
                            service.createBudget(request)
                        }

                        else -> {
                            val parameters = call.receiveParameters()
                            logger.info("Processing form request with parameters: ${parameters.names()}")

                            val userId = parameters["userId"]?.toIntOrNull()
                                ?: throw IllegalArgumentException("Valid user id is required")
                            val budgetName = parameters["budgetName"]?.takeIf { it.isNotBlank() }
                                ?: throw IllegalArgumentException("Budget name is required")
                            val totalIncome = parameters["totalIncome"]?.toDoubleOrNull()
                                ?: throw IllegalArgumentException("Valid total income is required")
                            val spreadsheetData = parameters["spreadsheetData"].orEmpty()

                            logger.debug("Parsing spreadsheet data: ${spreadsheetData.take(100)}...")
                            val (items, errors, totalAmount) = BudgetParser.parseSpreadsheetData(spreadsheetData = spreadsheetData)

                            if (errors.isNotEmpty()) {
                                logger.warn("Budget parsing errors encountered: $errors")
                                return@post call.respondText(
                                    ResponseComponents.error(errors.joinToString("<br>")), ContentType.Text.Html,
                                    HttpStatusCode.OK
                                )
                            }

                            logger.info("Creating budget for user $userId with ${items.size} items, total amount: $totalAmount")
                            val request = BudgetCreationRequest(
                                userId = userId, name = budgetName, totalIncome = totalIncome, totalExpenses = totalAmount
                            )

                            val newBudgetId = service.createBudget(request)
                            logger.info("Created budget with ID: $newBudgetId")

                            val updatedItems = items.map { item -> item.copy(budgetId = newBudgetId) }
                            logger.debug("Creating ${updatedItems.size} budget items")
                            budgetItemService.createBulkBudgetItems(budgetItems = updatedItems)
                            logger.info("Successfully created budget items for budget $newBudgetId")
                            newBudgetId
                        }
                    }

                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            logger.info("Sending JSON response for budget $budgetId")
                            call.respond(HttpStatusCode.Created, mapOf("id" to budgetId))
                        }

                        else -> {
                            logger.info("Sending HTML response for budget $budgetId")
                            call.respondText(
                                ResponseComponents.success("Budget Created"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }

                } catch (e: IllegalArgumentException) {
                    logger.warn("Validation error during budget creation: ${e.message}", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error(e.message ?: "Invalid request"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Unexpected error during budget creation", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.InternalServerError, "Error creating budget")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error creating budget"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                }
            }

            // Get budget by ID
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")

                    val budget = service.getBudget(id)
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
                    val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    val budgets = service.getUserBudgets(userId)
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
                    val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                    val startDate = call.parameters["startDate"]?.let { LocalDate.parse(it) } ?: throw IllegalArgumentException(
                        "Start date is required"
                    )
                    val endDate = call.parameters["endDate"]?.let { LocalDate.parse(it) } ?: throw IllegalArgumentException(
                        "End date is required"
                    )

                    val budgets = service.getUserBudgetsInDateRange(userId, startDate, endDate)
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
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")

                    val request = call.receive<UpdateBudgetRequest>()
                    val existingBudget = service.getBudget(id) ?: throw IllegalArgumentException("Budget not found")

                    val updatedBudget = existingBudget.copy(
                        name = request.name, description = request.description, startDate = request.startDate, endDate = request.endDate
                    )

                    service.updateBudget(id, updatedBudget)
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
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")

                    val request = call.receive<UpdateBudgetTotalsRequest>()
                    service.updateBudgetTotals(
                        id, BigDecimal.valueOf(request.totalIncome), BigDecimal.valueOf(request.totalExpenses)
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
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")
                    service.deleteBudget(id)

                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.OK, "Budget deleted successfully")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.success("Budget deleted successfully"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error(e.message ?: "Invalid request"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: Exception) {
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.InternalServerError, "Error deleting budget")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error deleting budget"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                }
            }

            // Delete all budgets for a user
            delete("/user/{userId}") {
                try {
                    val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    service.deleteUserBudgets(userId)
                    call.respond(HttpStatusCode.OK, "User budgets deleted successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting user budgets")
                }
            }
        }
    }
}