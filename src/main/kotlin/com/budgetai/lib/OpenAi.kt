package com.budgetai.lib

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.ktor.server.config.*
import io.ktor.server.engine.*

class OpenAi(config: ApplicationConfig) {
    private val apiKey: String = config.property("api-keys.openai").getString()
    private val client = OpenAI(apiKey)
    private val defaultModel = ModelId("gpt-4o-mini")

    // Send a single message and get response
    suspend fun sendMessage(prompt: String, model: ModelId = defaultModel): String {
        try {
            val request = ChatCompletionRequest(
                model = model, messages = listOf(ChatMessage(role = ChatRole.User, content = prompt))
            )

            val response: ChatCompletion = client.chatCompletion(request)
            return response.choices.firstOrNull()?.message?.content ?: "No response received"
        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    // Send conversation history and get response
    suspend fun sendConversation(messages: List<Pair<String, Boolean>>, model: ModelId = defaultModel): String {
        try {
            val chatMessages = messages.map { (content, isUser) ->
                ChatMessage(
                    role = if (isUser) ChatRole.User else ChatRole.Assistant, content = content
                )
            }

            val request = ChatCompletionRequest(
                model = model, messages = chatMessages
            )

            val response: ChatCompletion = client.chatCompletion(request)
            return response.choices.firstOrNull()?.message?.content ?: "No response received"
        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    class OpenAiException(message: String, cause: Throwable? = null) : Exception(message, cause)
}