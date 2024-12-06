package com.budgetai.routes.api

import com.budgetai.models.CookieConfig
import com.budgetai.models.UserAuthenticationRequest
import com.budgetai.models.UserCreationRequest
import com.budgetai.plugins.TOKEN_EXPIRATION
import com.budgetai.services.UserService
import com.budgetai.templates.components.ResponseComponents
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.date.*
import org.slf4j.LoggerFactory

private fun ApplicationCall.setAuthCookie(token: String, cookieConfig: CookieConfig) {
    response.cookies.append(
        Cookie(
            name = cookieConfig.name, value = token, maxAge = cookieConfig.maxAgeInSeconds, expires = null, domain = null,
            path = cookieConfig.path, secure = cookieConfig.secure, httpOnly = cookieConfig.httpOnly,
            extensions = mapOf("SameSite" to "Strict")
        )
    )
}

fun Route.authRoutes(service: UserService) {
    val logger = LoggerFactory.getLogger("AuthRoutes")

    val cookieConfig = CookieConfig(
        name = "jwt_token", maxAgeInSeconds = TOKEN_EXPIRATION, path = "/", secure = true, httpOnly = true
    )
    route("/auth") {
        // login
        post("/login") {
            try {
                val request = when (call.request.contentType()) {
                    ContentType.Application.Json -> {
                        // Handle JSON request
                        val jsonRequest = call.receive<UserAuthenticationRequest>()
                        // Return JSON response for API clients
                        val result = service.authenticateUserWithToken(jsonRequest)
                        if (result != null) {
                            val (_, token) = result
                            call.setAuthCookie(token, cookieConfig)
                            call.respond(HttpStatusCode.OK, mapOf("message" to "Login successful", "redirectUrl" to "/dashboard"))
                        }
                        return@post
                    }

                    ContentType.Application.FormUrlEncoded -> {
                        // Handle form data
                        val parameters = call.receiveParameters()
                        UserAuthenticationRequest(
                            email = parameters["email"] ?: throw IllegalArgumentException("Email is required"),
                            password = parameters["password"] ?: throw IllegalArgumentException("Password is required")
                        )
                    }

                    else -> throw IllegalArgumentException("Unsupported content type")
                }

                // Process form-based authentication
                val result = service.authenticateUserWithToken(request)
                if (result != null) {
                    val (_, token) = result
                    call.setAuthCookie(token, cookieConfig)

                    val response = ResponseComponents.success(
                        "Login successful! Redirecting..."
                    ) + """<script>window.location.href = '/dashboard';</script>""".trimIndent()

                    call.respondText(response, ContentType.Text.Html)
                }

            } catch (e: Exception) {
                logger.error("Login failed", e)
                val errorMessage = when (e) {
                    is IllegalArgumentException -> e.message
                    else -> "Invalid login credentials"
                }

                // Return appropriate error format based on content type
                when (call.request.contentType()) {
                    ContentType.Application.Json -> {
                        call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to (errorMessage ?: "An unknown error occurred"))
                        )
                    }

                    else -> {
                        val errorResponse = ResponseComponents.error(errorMessage ?: "An unknown error occurred")
                        logger.debug("Generated error response: $errorResponse")
                        call.respondText(
                            errorResponse, ContentType.Text.Html, HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }

        // Create new user
        post("/register") {
            try {
                val request = when (call.request.contentType()) {
                    ContentType.Application.Json -> {
                        // Handle JSON request
                        val jsonRequest = call.receive<UserCreationRequest>()
                        val userId= service.createUser(jsonRequest)
                        val authRequest = UserAuthenticationRequest(jsonRequest.email, jsonRequest.password)
                        val result = service.authenticateUserWithToken(authRequest)
                        if (result != null) {
                            val (_, token) = result
                            call.setAuthCookie(token, cookieConfig)
                            call.respond(
                                HttpStatusCode.OK, mapOf(
                                    "userid" to userId
                                )
                            )
                        }
                        return@post
                    }

                    ContentType.Application.FormUrlEncoded -> {
                        val parameters = call.receiveParameters()
                        UserCreationRequest(
                            name = parameters["name"] ?: throw IllegalArgumentException("Name is required"),
                            email = parameters["email"] ?: throw IllegalArgumentException("Email is required"),
                            password = parameters["password"] ?: throw IllegalArgumentException("Password is required")
                        )
                    }

                    else -> throw IllegalArgumentException("Unsupported content type")
                }

                // Process form-based registration
                service.createUser(request)
                val authRequest = UserAuthenticationRequest(request.email, request.password)
                val result = service.authenticateUserWithToken(authRequest)
                if (result != null) {
                    val (_, token) = result
                    call.setAuthCookie(token, cookieConfig)
                }
                val response = ResponseComponents.success("Registration successful! Redirecting...")
                call.respondText(response, ContentType.Text.Html)

            } catch (e: Exception) {
                logger.error("Registration failed", e)
                val errorMessage = when (e) {
                    is IllegalArgumentException -> e.message
                    else -> "Registration failed. Please try again."
                }

                when (call.request.contentType()) {
                    ContentType.Application.Json -> {
                        call.respond(
                            HttpStatusCode.BadRequest, mapOf("error" to (errorMessage ?: "An unknown error occurred"))
                        )
                    }

                    else -> {
                        val errorResponse = ResponseComponents.error(errorMessage ?: "An unknown error occurred")
                        logger.debug("Generated error response: $errorResponse")
                        call.respondText(
                            errorResponse, ContentType.Text.Html, HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }

        authenticate {
            // refresh token
            post("/refresh") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("userId", String::class)?.toIntOrNull()

                if (userId != null) {
                    val newToken = service.refreshToken(userId)
                    if (newToken != null) {
                        call.setAuthCookie(newToken, cookieConfig)
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
                call.response.headers.append(HttpHeaders.Location, "/auth")
                call.respond(HttpStatusCode.Found)
            }
        }

    }
}