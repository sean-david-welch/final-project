package com.budgetai.routes

import com.budgetai.models.*
import com.budgetai.repositories.AiInsightRepository
import com.budgetai.services.AiInsightService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Database

fun Route.aiInsightRoutes(database: Database) {
    val aiInsightRepository = AiInsightRepository(database)
    val aiInsightService = AiInsightService(aiInsightRepository)

    route("/ai-insights") {
        // Create new insight
        post {
            try {
                val request = call.receive<InsightCreationRequest>()
                val insightId = aiInsightService.createInsight(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to insightId))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating insight: ${e.message}")
            }
        }

        // Get insight by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid insight ID")

                val insight = aiInsightService.getInsight(id)
                if (insight != null) {
                    call.respond(insight)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Insight not found")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insight")
            }
        }

        // Get user's insights
        get("/user/{userId}") {
            try {
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val insights = aiInsightService.getUserInsights(userId)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get insights for a budget
        get("/budget/{budgetId}") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")

                val insights = aiInsightService.getBudgetInsights(budgetId)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get insights for a budget item
        get("/budget-item/{budgetItemId}") {
            try {
                val budgetItemId = call.parameters["budgetItemId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")

                val insights = aiInsightService.getBudgetItemInsights(budgetItemId)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get insights by type
        get("/type/{type}") {
            try {
                val type = InsightType.valueOf(
                    call.parameters["type"] ?: throw IllegalArgumentException("Invalid insight type")
                )

                val insights = aiInsightService.getInsightsByType(type)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get insights by sentiment
        get("/sentiment/{sentiment}") {
            try {
                val sentiment = Sentiment.valueOf(
                    call.parameters["sentiment"] ?: throw IllegalArgumentException("Invalid sentiment")
                )

                val insights = aiInsightService.getInsightsBySentiment(sentiment)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get insights in date range
        get("/user/{userId}/date-range") {
            try {
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                val startDate = LocalDateTime.parse(
                    call.parameters["startDate"] ?: throw IllegalArgumentException("Start date required")
                )
                val endDate = LocalDateTime.parse(
                    call.parameters["endDate"] ?: throw IllegalArgumentException("End date required")
                )

                val insights = aiInsightService.getInsightsInDateRange(userId, startDate, endDate)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Get user insight analytics
        get("/user/{userId}/analytics") {
            try {
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                val analytics = aiInsightService.getUserInsightAnalytics(userId)
                call.respond(analytics)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving analytics")
            }
        }

        // Get paginated recent insights
        get("/user/{userId}/recent") {
            try {
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10

                val insights = aiInsightService.getRecentInsightsPaginated(userId, page, pageSize)
                call.respond(insights)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving insights")
            }
        }

        // Update insight
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid insight ID")
                val request = call.receive<InsightUpdateRequest>()

                aiInsightService.updateInsight(id, request)
                call.respond(HttpStatusCode.OK, "Insight updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating insight")
            }
        }

        // Update insight sentiment
        put("/{id}/sentiment") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid insight ID")
                val request = call.receive<UpdateSentimentRequest>()

                aiInsightService.updateInsightSentiment(id, request.sentiment)
                call.respond(HttpStatusCode.OK, "Sentiment updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating sentiment")
            }
        }

        // Update insight metadata
        put("/{id}/metadata") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid insight ID")
                val request = call.receive<UpdateMetadataRequest>()

                aiInsightService.updateInsightMetadata(id, request.metadata)
                call.respond(HttpStatusCode.OK, "Metadata updated successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating metadata")
            }
        }

        // Delete insight
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid insight ID")

                aiInsightService.deleteInsight(id)
                call.respond(HttpStatusCode.OK, "Insight deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting insight")
            }
        }

        // Delete all insights for a user
        delete("/user/{userId}") {
            try {
                val userId =
                    call.parameters["userId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid user ID")

                aiInsightService.deleteUserInsights(userId)
                call.respond(HttpStatusCode.OK, "User insights deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting user insights")
            }
        }

        // Delete all insights for a budget
        delete("/budget/{budgetId}") {
            try {
                val budgetId =
                    call.parameters["budgetId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid budget ID")

                aiInsightService.deleteBudgetInsights(budgetId)
                call.respond(HttpStatusCode.OK, "Budget insights deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget insights")
            }
        }

        // Delete all insights for a budget item
        delete("/budget-item/{budgetItemId}") {
            try {
                val budgetItemId = call.parameters["budgetItemId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid budget item ID")

                aiInsightService.deleteBudgetItemInsights(budgetItemId)
                call.respond(HttpStatusCode.OK, "Budget item insights deleted successfully")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting budget item insights")
            }
        }
    }
}