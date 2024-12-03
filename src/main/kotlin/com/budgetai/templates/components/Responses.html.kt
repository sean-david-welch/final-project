package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

// Component for rendering messages
fun DIV.messageComponent(message: String, type: String) {
    div {
        attributes["class"] = when (type) {
            "success" -> "success-message animate-fade-in"
            "error" -> "error-message animate-fade-in"
            else -> "$type-message"
        }

        // Optional: Add icon spans
        span {
            attributes["class"] = "message-content"
            +message
        }
    }
}


// Reusable response components
object ResponseComponents {
    fun success(message: String) = createHTML().div {
        messageComponent(message, "success")
    }

    fun error(message: String) = createHTML().div {
        messageComponent(message, "error")
    }
}