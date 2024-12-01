package com.budgetai.lib

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.runBlocking

class OpenAi(private val apiKey: String) {
    private val client = OpenAI(apiKey)

    // Default model, can be overridden in methods
    private val defaultModel = ModelId("gpt-4")

    /**
     * Sends a simple message to the API and returns the response
     * @param prompt The user's message
     * @param model Optional model ID, defaults to gpt-4
     * @return The AI's response as a string
     */
    fun sendMessage(prompt: String, model: ModelId = defaultModel): String = runBlocking {
        try {
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                )
            )

            val response: ChatCompletion = client.chatCompletion(request)
            return@runBlocking response.choices.firstOrNull()?.message?.content ?: "No response received"

        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    /**
     * Sends a conversation history to the API and returns the response
     * @param messages List of previous messages in the conversation
     * @param model Optional model ID, defaults to gpt-4
     * @return The AI's response as a string
     */
    fun sendConversation(messages: List<Pair<String, Boolean>>, model: ModelId = defaultModel): String = runBlocking {
        try {
            val chatMessages = messages.map { (content, isUser) ->
                ChatMessage(
                    role = if (isUser) ChatRole.User else ChatRole.Assistant,
                    content = content
                )
            }

            val request = ChatCompletionRequest(
                model = model,
                messages = chatMessages
            )

            val response: ChatCompletion = client.chatCompletion(request)
            return@runBlocking response.choices.firstOrNull()?.message?.content ?: "No response received"

        } catch (e: Exception) {
            throw OpenAiException("Failed to get response from OpenAI: ${e.message}", e)
        }
    }

    /**
     * Custom exception for OpenAI-related errors
     */
    class OpenAiException(message: String, cause: Throwable? = null) : Exception(message, cause)
}