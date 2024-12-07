package com.budgetai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String, val content: String
)

@Serializable
data class ChatRequest(
    val model: String, val messages: List<ChatMessage>
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(
        val message: ChatMessage, @SerialName("finish_reason") val finishReason: String
    )
}