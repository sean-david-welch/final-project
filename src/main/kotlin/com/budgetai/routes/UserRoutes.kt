package com.budgetai.routes

import com.budgetai.models.*
import com.budgetai.repositories.UserRepository
import com.budgetai.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

fun Route.userRoutes(database: Database) {
    // Initialize repositories and services
    val userRepository = UserRepository(database)
    val userService = UserService(userRepository)

    route("/users") {
        // Authentication Routes

        post("/login") {
            try {
                val request = call.receive<UserAuthenticationRequest>()
                val user = userService.authenticateUser(request)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        // User Management Routes

        // Create new user
        post("/register") {
            try {
                val request = call.receive<UserCreationRequest>()
                val userId = userService.createUser(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to userId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating user")
            }
        }

        // Get user by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val user = userService.getUser(id)
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

                val user = userService.getUserByEmail(email)
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
                    id = id, email = request.email, name = request.name
                )

                userService.updateUser(id, userDTO)
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
                userService.updatePassword(id, request.currentPassword, request.newPassword)
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

                userService.deleteUser(id)
                call.respond(HttpStatusCode.OK, "User deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting user")
            }
        }
    }
}