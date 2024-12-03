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
            val parameters = call.receiveParameters()
            val request = UserAuthenticationRequest(
                email = parameters["email"] ?: throw IllegalArgumentException("Email is required"),
                password = parameters["password"] ?: throw IllegalArgumentException("Password is required")
            )

            val result = service.authenticateUserWithToken(request)
            if (result != null) {
                val (_, token) = result
                call.setAuthCookie(token, cookieConfig)

                val response = ResponseComponents.success("Login successful! Redirecting...") + """
                    <script>
                        window.location.href = '/dashboard';
                    </script>
                """.trimIndent()

                call.respondText(
                    response,
                    ContentType.Text.Html
                )
            }
        } catch (e: Exception) {
            logger.error("Login failed", e)

            val errorMessage = when (e) {
                is IllegalArgumentException -> e.message
                else -> "Invalid login credentials"
            }

            val errorResponse = ResponseComponents.error(errorMessage ?: "An unknown error occurred")
            logger.debug("Generated error response: $errorResponse")

            call.respondText(
                errorResponse,
                ContentType.Text.Html,
                HttpStatusCode.BadRequest
            )
        }
    }

        // Create new user
        post("/register") {
            try {
                val parameters = call.receiveParameters()
                val request = UserCreationRequest(
                    name = parameters["name"] ?: throw IllegalArgumentException("Name is required"),
                    email = parameters["email"] ?: throw IllegalArgumentException("Email is required"),
                    password = parameters["password"] ?: throw IllegalArgumentException("Password is required"),
                )
                service.createUser(request)
                val authRequest = UserAuthenticationRequest(request.email, request.password)
                service.authenticateUserWithToken(authRequest)
                val response = ResponseComponents.success("Registration successful! Redirecting...")
                call.respondText(
                    response, ContentType.Text.Html
                )
            } catch (e: Exception) {
                logger.error("Registration failed", e)
                val errorMessage = when (e) {
                    is IllegalArgumentException -> e.message
                    else -> "Registration failed. Please try again."
                }
                val errorResponse = ResponseComponents.error(errorMessage ?: "An unknown error occurred")
                logger.debug("Generated error response: $errorResponse")
                call.respondText(
                    errorResponse, ContentType.Text.Html, HttpStatusCode.BadRequest
                )
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
                call.respond(HttpStatusCode.OK, "Logged out successfully")
            }
        }

    }
}