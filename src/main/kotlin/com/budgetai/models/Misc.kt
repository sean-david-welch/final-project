package com.budgetai.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String, val content: String
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 500,
    val temperature: Double = 0.7,
    @SerialName("top_p") val topP: Double = 1.0,
    val stream: Boolean = false
)

@Serializable
data class ChatResponse(
    val id: String? = null, @SerialName("object") val objectName: String? = null, val created: Long? = null, val model: String? = null,
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(
        val index: Int? = null, val message: ChatMessage, @SerialName("finish_reason") val finishReason: String? = null
    )
}