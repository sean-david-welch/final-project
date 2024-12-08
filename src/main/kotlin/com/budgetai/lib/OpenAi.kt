package com.budgetai.lib

import com.budgetai.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json

class OpenAi(config: ApplicationConfig) {
    private val apiKey: String = config.property("api-keys.openai").getString()
    private val defaultModel = "gpt-4-mini"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        expectSuccess = true  // This will throw on non-2xx responses
    }

    suspend fun sendMessage(prompt: String, model: String = defaultModel): String {
        if (prompt.isBlank()) {
            throw OpenAiException("Prompt cannot be empty")
        }

        try {
            val response = client.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(
                    ChatRequest(
                        model = model,
                        messages = listOf(ChatMessage(role = "user", content = prompt))
                    )
                )
            }

            val chatResponse = response.body<ChatResponse>()
            return chatResponse.choices.firstOrNull()?.message?.content
                ?: throw OpenAiException("No response content received")

        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    class OpenAiException(message: String, cause: Throwable? = null) : Exception(message, cause)
}