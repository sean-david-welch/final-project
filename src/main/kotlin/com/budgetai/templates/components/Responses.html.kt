package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

// Component for rendering messages
fun DIV.messageComponent(message: String, type: String) {
    div {
        attributes["class"] = "$type-message"
        +message
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