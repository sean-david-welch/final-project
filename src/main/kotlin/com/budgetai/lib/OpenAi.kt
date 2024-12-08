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
import org.slf4j.LoggerFactory

class OpenAi(config: ApplicationConfig) {
    private val logger = LoggerFactory.getLogger("OpenAI")
    private val apiKey: String = config.property("api-keys.openai").getString()
    private val defaultModel = "gpt-4o-mini"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        expectSuccess = true
    }

    suspend fun sendMessage(prompt: String, model: String = defaultModel): String {
        logger.info("Preparing to send message to OpenAI API")
        logger.debug("Using model: $model")

        if (prompt.isBlank()) {
            logger.error("Empty prompt provided")
            throw OpenAiException("Prompt cannot be empty")
        }

        try {
            logger.debug("Sending request to OpenAI API")
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

            logger.debug("Received response from OpenAI API")
            val chatResponse = response.body<ChatResponse>()

            chatResponse.choices.firstOrNull()?.message?.content?.let { content ->
                logger.info("Successfully received response from OpenAI")
                logger.debug("Response length: ${content.length} characters")
                return content
            } ?: run {
                logger.error("No content in OpenAI response")
                throw OpenAiException("No response content received")
            }

        } catch (e: Exception) {
            logger.error("Failed to get response from OpenAI", e)
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        } finally {
            logger.debug("Completed OpenAI API request")
        }
    }

    init {
        logger.info("Initializing OpenAI client with model: $defaultModel")
    }

    class OpenAiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
        init {
            LoggerFactory.getLogger(OpenAiException::class.java)
                .error("OpenAI Exception: $message", cause)
        }
    }
}