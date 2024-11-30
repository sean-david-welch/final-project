package com.budgetai.routes.api

import com.budgetai.models.*
import com.budgetai.routes.middleware.authenticate
import com.budgetai.services.SavingsGoalService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.savingsGoalRoutes(service: SavingsGoalService) {
        // protected routes
        authenticate {
            route("/api/savings-goals") {
                // Create new savings goal
                post {
                    try {
                        val request = call.receive<SavingsGoalCreationRequest>()
                        val goalId = service.createSavingsGoal(request)
                        call.respond(HttpStatusCode.Created, mapOf("id" to goalId))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error creating savings goal")
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