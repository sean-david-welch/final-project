package com.budgetai.routes.api

import com.budgetai.models.*
import com.budgetai.services.SavingsGoalService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.savingsGoalRoutes(service: SavingsGoalService) {
        // protected routes
        authenticate {
            route("/api/savings-goals") {
                // Create new savings goal
// Create new savings goal
                post {
                    try {
                        val request = when (call.request.contentType()) {
                            ContentType.Application.Json -> {
                                call.receive<SavingsGoalCreationRequest>()
                            }
                            ContentType.Application.FormUrlEncoded -> {
                                val params = call.receiveParameters()
                                SavingsGoalCreationRequest(
                                    userId = params["userId"]?.toInt() ?: throw IllegalArgumentException("User ID is required"),
                                    name = params["name"] ?: throw IllegalArgumentException("Name is required"),
                                    description = params["description"],
                                    targetAmount = params["targetAmount"]?.toBigDecimalOrNull()
                                        ?: throw IllegalArgumentException("Target amount is required"),
                                    currentAmount = params["currentAmount"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    targetDate = params["targetDate"]
                                )
                            }
                            else -> throw IllegalArgumentException("Unsupported content type")
                        }

                        val goalId = service.createSavingsGoal(request)

                        when (call.request.contentType()) {
                            ContentType.Application.Json -> {
                                call.respond(HttpStatusCode.Created, mapOf("id" to goalId))
                            }
                            else -> {
                                call.respondText(
                                    ResponseComponents.success("Savings goal created successfully"),
                                    ContentType.Text.Html,
                                    HttpStatusCode.OK
                                )
                            }
                        }
                    } catch (e: IllegalArgumentException) {
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
                        when (call.request.contentType()) {
                            ContentType.Application.Json -> {
                                call.respond(HttpStatusCode.InternalServerError, "Error creating savings goal")
                            }
                            else -> {
                                call.respondText(
                                    ResponseComponents.error("Error creating savings goal"),
                                    ContentType.Text.Html,
                                    HttpStatusCode.OK
                                )
                            }
                        }
                    }
                }

                // Get savings goal by ID
                get("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")

                        val goal = service.getSavingsGoal(id)
                        if (goal != null) {
                            call.respond(goal)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Savings goal not found")
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving savings goal")
                    }
                }

                // Get goal progress
                get("/{id}/progress") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")

                        val progress = service.getGoalProgress(id)
                        call.respond(progress)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving goal progress")
                    }
                }

                // Get all user's savings goals
                get("/user/{userId}") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                        val goals = service.getUserSavingsGoals(userId)
                        call.respond(goals)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving savings goals")
                    }
                }

                // Get active savings goals
                get("/user/{userId}/active") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                        val goals = service.getActiveSavingsGoals(userId)
                        call.respond(goals)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving active goals")
                    }
                }

                // Get completed savings goals
                get("/user/{userId}/completed") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                        val goals = service.getCompletedSavingsGoals(userId)
                        call.respond(goals)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving completed goals")
                    }
                }

                // Get upcoming savings goals
                get("/user/{userId}/upcoming") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                        val goals = service.getUpcomingSavingsGoals(userId)
                        call.respond(goals)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error retrieving upcoming goals")
                    }
                }

                // Get total user savings
                get("/user/{userId}/total") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                        val total = service.getTotalUserSavings(userId)
                        call.respond(mapOf("total" to total))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error calculating total savings")
                    }
                }

                // Update savings goal
                put("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                        val request = call.receive<SavingsGoalUpdateRequest>()
                        service.updateSavingsGoal(id, request)
                        call.respond(HttpStatusCode.OK, "Savings goal updated successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error updating savings goal")
                    }
                }

                // Add contribution
                post("/{id}/contribute") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                        val request = call.receive<ContributionRequest>()
                        service.addContribution(id, request.amount)
                        call.respond(HttpStatusCode.OK, "Contribution added successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error adding contribution")
                    }
                }

                // Withdraw amount
                post("/{id}/withdraw") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                        val request = call.receive<WithdrawalRequest>()
                        service.withdrawAmount(id, request.amount)
                        call.respond(HttpStatusCode.OK, "Withdrawal processed successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error processing withdrawal")
                    }
                }

                // Update current amount
                put("/{id}/current-amount") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                        val request = call.receive<UpdateCurrentAmountRequest>()
                        service.updateCurrentAmount(id, request.amount)
                        call.respond(HttpStatusCode.OK, "Current amount updated successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error updating current amount")
                    }
                }

                // Delete savings goal
                delete("/{id}") {
                    try {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                        service.deleteSavingsGoal(id)
                        call.respond(HttpStatusCode.OK, "Savings goal deleted successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error deleting savings goal")
                    }
                }

                // Delete all user's savings goals
                delete("/user/{userId}") {
                    try {
                        val userId = call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                        service.deleteUserSavingsGoals(userId)
                        call.respond(HttpStatusCode.OK, "User savings goals deleted successfully")
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error deleting user savings goals")
                    }
                }
            }
        }
}