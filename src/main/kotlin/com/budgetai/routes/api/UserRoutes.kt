package com.budgetai.routes.api

import com.budgetai.models.UpdatePasswordRequest
import com.budgetai.models.UpdateUserRequest
import com.budgetai.models.UserCreationRequest
import com.budgetai.models.UserDTO
import com.budgetai.routes.middleware.authenticate
import com.budgetai.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(service: UserService) {

    route("/api/users") {
        // Create new user
        post("/register") {
            try {
                val request = call.receive<UserCreationRequest>()
                val userId = service.createUser(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to userId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating user")
            }
        }

        // proteted routes
        authenticate("jwt-auth") {
            // Get user by ID
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    val user = service.getUser(id)
                    if (user != null) {
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error retrieving user")
                }
            }

            // Get user by email
            get("/email/{email}") {
                try {
                    val email = call.parameters["email"] ?: throw IllegalArgumentException("Email is required")

                    val user = service.getUserByEmail(email)
                    if (user != null) {
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error retrieving user")
                }
            }

            // Update user information
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    val request = call.receive<UpdateUserRequest>()
                    val userDTO = UserDTO(
                        id = id, email = request.email, name = request.name, role = request.role
                    )

                    service.updateUser(id, userDTO)
                    call.respond(HttpStatusCode.OK, "User updated successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating user")
                }
            }

            // Update password
            put("/{id}/password") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    val request = call.receive<UpdatePasswordRequest>()
                    service.updatePassword(id, request.currentPassword, request.newPassword)
                    call.respond(HttpStatusCode.OK, "Password updated successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating password")
                }
            }

            // Delete user
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    service.deleteUser(id)
                    call.respond(HttpStatusCode.OK, "User deleted successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting user")
                }
            }
        }
    }
}