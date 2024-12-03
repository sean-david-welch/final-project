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
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json

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
    val cookieConfig = CookieConfig(
        name = "jwt_token", maxAgeInSeconds = TOKEN_EXPIRATION, path = "/", secure = true, httpOnly = true
    )
    route("/auth") {
        // login
        post("/login") {
            try {
                val request = call.receive<UserAuthenticationRequest>()
                println("Parsed request: $request")
                val result = service.authenticateUserWithToken(request)
                println("Authentication result: $result")

                if (result != null) {
                    val (_, token) = result
                    call.setAuthCookie(token, cookieConfig)
                    call.respondText(
                        """
                        <div class="success-message">
                            Login successful! Redirecting...
                        </div>
                        <script>
                            window.location.href = '/dashboard';
                        </script>
                        """.trimIndent(), ContentType.Text.Html
                    )
                }
            } catch (e: Exception) {
                call.respondText(
                    """
                    <div class="error-message">
                        ${e.message ?: "Invalid login credentials"}
                    </div>
                    """.trimIndent(), ContentType.Text.Html, HttpStatusCode.BadRequest
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
                    role = parameters["role"] ?: "USER"
                )

                service.createUser(request)
                val authRequest = UserAuthenticationRequest(request.email, request.password)
                service.authenticateUserWithToken(authRequest)

                call.respondText(
                    ResponseComponents.success("Registration successful! Redirecting..."), ContentType.Text.Html
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IllegalArgumentException -> e.message
                    else -> "Registration failed. Please try again."
                }

                call.respondText(
                    ResponseComponents.error(errorMessage ?: "An unknown error occurred"), ContentType.Text.Html, HttpStatusCode.BadRequest
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