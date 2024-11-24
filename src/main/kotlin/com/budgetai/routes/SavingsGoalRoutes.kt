package com.budgetai.routes

import com.budgetai.repositories.SavingsGoalRepository
import com.budgetai.services.SavingsGoalService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

fun Route.savingsGoalRoutes(database: Database) {
    val savingsGoalRepository = SavingsGoalRepository(database)
    val savingsGoalService = SavingsGoalService(savingsGoalRepository)

    @Serializable
    data class ContributionRequest(
        val amount: Double
    )

    @Serializable
    data class WithdrawalRequest(
        val amount: Double
    )

    @Serializable
    data class UpdateCurrentAmountRequest(
        val amount: Double
    )

    route("/savings-goals") {
        // Create new savings goal
        post {
            try {
                val request = call.receive<SavingsGoalService.SavingsGoalCreationRequest>()
                val goalId = savingsGoalService.createSavingsGoal(request)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")

                val goal = savingsGoalService.getSavingsGoal(id)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")

                val progress = savingsGoalService.getGoalProgress(id)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val goals = savingsGoalService.getUserSavingsGoals(userId)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val goals = savingsGoalService.getActiveSavingsGoals(userId)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val goals = savingsGoalService.getCompletedSavingsGoals(userId)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val goals = savingsGoalService.getUpcomingSavingsGoals(userId)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                val total = savingsGoalService.getTotalUserSavings(userId)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                val request = call.receive<SavingsGoalService.SavingsGoalUpdateRequest>()
                savingsGoalService.updateSavingsGoal(id, request)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                val request = call.receive<ContributionRequest>()
                savingsGoalService.addContribution(id, request.amount)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                val request = call.receive<WithdrawalRequest>()
                savingsGoalService.withdrawAmount(id, request.amount)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                val request = call.receive<UpdateCurrentAmountRequest>()
                savingsGoalService.updateCurrentAmount(id, request.amount)
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
                val id =
                    call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid savings goal ID")
                savingsGoalService.deleteSavingsGoal(id)
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
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                savingsGoalService.deleteUserSavingsGoals(userId)
                call.respond(HttpStatusCode.OK, "User savings goals deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting user savings goals")
            }
        }
    }
}