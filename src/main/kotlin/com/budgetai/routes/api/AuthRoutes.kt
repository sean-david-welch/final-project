package com.budgetai.routes.api

import com.budgetai.models.CookieConfig
import com.budgetai.models.UserAuthenticationRequest
import com.budgetai.models.UserCreationRequest
import com.budgetai.plugins.TOKEN_EXPIRATION
import com.budgetai.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*

private fun ApplicationCall.setAuthCookie(token: String, cookieConfig: CookieConfig) {
    response.cookies.append(
        Cookie(
            name = cookieConfig.name,
            value = token,
            maxAge = cookieConfig.maxAgeInSeconds,
            expires = null,
            domain = null,
            path = cookieConfig.path,
            secure = cookieConfig.secure,
            httpOnly = cookieConfig.httpOnly,
            extensions = mapOf("SameSite" to "Strict")
        )
    )
}

fun Route.authRoutes(service: UserService) {
    val cookieConfig = CookieConfig(
        name = "jwt_token", maxAgeInSeconds = TOKEN_EXPIRATION, path = "/", secure = true, httpOnly = true
    )


    route("/auth") {
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

        // Authentication Routes
        post("/login") {
            try {
                val request = call.receive<UserAuthenticationRequest>()
                val result = service.authenticateUserWithToken(request)
                if (result != null) {
                    val (user, token) = result
                    call.response.cookies.append(
                        Cookie(
                            name = cookieConfig.name, value = token, maxAge = cookieConfig.maxAgeInSeconds, expires = null, domain = null,
                            path = cookieConfig.path, secure = cookieConfig.secure, httpOnly = cookieConfig.httpOnly,
                            extensions = mapOf("SameSite" to "Strict")
                        )
                    )
                    call.respond(HttpStatusCode.OK, hashMapOf("user" to user))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            }
        }

        authenticate("auth-jwt") {
            // refresh token
            post("/refresh") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toIntOrNull()

                if (userId != null) {
                    val newToken = service.refreshToken(userId)
                    if (newToken != null) {
                        call.response.cookies.append(
                            Cookie(
                                name = cookieConfig.name, value = newToken, maxAge = cookieConfig.maxAgeInSeconds, expires = null,
                                domain = null, path = cookieConfig.path, secure = cookieConfig.secure, httpOnly = cookieConfig.httpOnly,
                                extensions = mapOf("SameSite" to "Strict")
                            )
                        )
                        call.respond(HttpStatusCode.OK, "Token refreshed")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                }
            }

            post("/logout") {
                // Clear the JWT cookie
                call.response.cookies.append(
                    Cookie(
                        name = cookieConfig.name, value = "", maxAge = 0, expires = GMTDate(0), domain = null, path = cookieConfig.path,
                        secure = cookieConfig.secure, httpOnly = cookieConfig.httpOnly
                    )
                )
                call.respond(HttpStatusCode.OK, "Logged out successfully")
            }
        }

    }
}