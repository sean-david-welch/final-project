package com.budgetai.routes

import com.budgetai.models.User
import com.budgetai.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.userRoutes(userService: UserService) {
    route("/users") {
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val user = userService.getUser(id)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            }
        }

        post("/") {
            val user = call.receive<User>()
            val userId = userService.createUser(user)
            call.respond(HttpStatusCode.Created, "User created with ID: $userId")
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val user = call.receive<User>()
                userService.updateUser(id, user)
                call.respond(HttpStatusCode.OK, "User updated")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                userService.deleteUser(id)
                call.respond(HttpStatusCode.OK, "User deleted")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            }
        }
    }
}