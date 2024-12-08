package com.budgetai.lib

import com.budgetai.models.ChatMessage
import com.budgetai.models.ChatRequest
import com.budgetai.models.ChatResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*

class OpenAi(config: ApplicationConfig) {
    private val apiKey: String = config.property("api-keys.openai").getString()
    private val defaultModel = "gpt-4o-mini"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // Send a single message and get response
    suspend fun sendMessage(prompt: String, model: String = defaultModel): String {
        try {
            val response = client.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(
                    ChatRequest(
                        model = model, messages = listOf(ChatMessage(role = "user", content = prompt))
                    )
                )
            }

            val chatResponse = response.body<ChatResponse>()
            return chatResponse.choices.firstOrNull()?.message?.content ?: "No response received"
        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    class OpenAiException(message: String, cause: Throwable? = null) : Exception(message, cause)
}