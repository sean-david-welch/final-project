package com.budgetai.routes.api

import com.budgetai.models.UpdatePasswordRequest
import com.budgetai.models.UpdateUserRequest
import com.budgetai.models.UserDTO
import com.budgetai.models.UserRole
import com.budgetai.services.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(service: UserService) {
    // proteted routes
    authenticate {
        route("/api/users") {
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

                    val existingUser = service.getUser(id) ?: throw IllegalArgumentException("User not found")
                    val request = call.receive<UpdateUserRequest>()
                    val role = if (existingUser.role == UserRole.ADMIN.toString()) UserRole.ADMIN.toString() else request.role
                    val userDTO = UserDTO(
                        id = id, email = request.email, name = request.name, role = role
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