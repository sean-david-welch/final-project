package com.budgetai.routes.api

import com.budgetai.models.*
import com.budgetai.services.UserService
import com.budgetai.templates.components.ResponseComponents
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.userRoutes(service: UserService) {
    val logger = LoggerFactory.getLogger("UserRoutes")

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

                    val request = when (call.request.contentType()) {
                        ContentType.Application.Json -> call.receive<UpdateUserRequest>()
                        ContentType.Application.FormUrlEncoded -> {
                            val parameters = call.receiveParameters()
                            UpdateUserRequest(
                                name = parameters["name"] ?: throw IllegalArgumentException("Name is required"),
                                email = parameters["email"] ?: throw IllegalArgumentException("Email is required"),
                                password = parameters["password"]
                            )
                        }

                        else -> throw IllegalArgumentException("Unsupported content type")
                    }

                    if (!request.password.isNullOrEmpty()) {
                        try {
                            service.updatePassword(id, request.password)
                        } catch (e: IllegalArgumentException) {
                            when (call.request.contentType()) {
                                ContentType.Application.Json -> {
                                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                                }

                                else -> {
                                    call.respondText(
                                        ResponseComponents.error(e.message ?: "Invalid password"), ContentType.Text.Html, HttpStatusCode.OK
                                    )
                                }
                            }
                            return@put
                        }
                    }

                    val role = if (existingUser.role == UserRole.ADMIN.toString()) UserRole.ADMIN.toString() else UserRole.USER.toString()
                    val userDTO = UserDTO(
                        id = id, email = request.email, name = request.name, role = role
                    )

                    service.updateUser(id, userDTO)

                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "User updated successfully"))
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.success("User updated successfully"), ContentType.Text.Html, HttpStatusCode.OK
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
                                ResponseComponents.error(e.message ?: "Invalid request"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error updating user", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error updating user"))
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error updating user"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                }
            }

            // update user role
            put("/{id}/role") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                    val existingUser = service.getUser(id) ?: throw IllegalArgumentException("User not found")

                    val request = when (call.request.contentType()) {
                        ContentType.Application.Json -> call.receive<UpdateRoleRequest>()
                        ContentType.Application.FormUrlEncoded -> {
                            val parameters = call.receiveParameters()
                            UpdateRoleRequest(
                                role = parameters["role"] ?: throw IllegalArgumentException("Role is required")
                            )
                        }

                        else -> throw IllegalArgumentException("Unsupported content type")
                    }

                    val userDTO = UserDTO(
                        id = existingUser.id,
                        email = existingUser.email,
                        name = existingUser.name,
                        role = request.role,
                    )

                    service.updateUser(id, userDTO)

                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.OK, mapOf("message" to "User role updated successfully"))
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.success("User role updated successfully"), ContentType.Text.Html, HttpStatusCode.OK
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
                                ResponseComponents.error(e.message ?: "Invalid request"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error updating user role", e)
                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error updating user role"))
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error updating user role"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                }
            }

            // Delete user
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                    service.deleteUser(id)

                    when (call.request.contentType()) {
                        ContentType.Application.Json -> {
                            call.respond(HttpStatusCode.OK, "User deleted successfully")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.success("User deleted successfully"), ContentType.Text.Html, HttpStatusCode.OK
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
                            call.respond(HttpStatusCode.InternalServerError, "Error deleting user")
                        }

                        else -> {
                            call.respondText(
                                ResponseComponents.error("Error deleting user"), ContentType.Text.Html, HttpStatusCode.OK
                            )
                        }
                    }
                }
            }

            // Update password
            put("/{id}/password") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                    val request = call.receive<UpdatePasswordRequest>()
                    service.updatePassword(id, request.newPassword)
                    call.respond(HttpStatusCode.OK, "Password updated successfully")
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating password")
                }
            }

        }
    }
}