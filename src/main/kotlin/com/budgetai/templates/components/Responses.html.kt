package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

// Component for rendering messages
fun DIV.messageComponent(message: String, type: String) {
    div {
        attributes["x-data"] = "{show: true}"
        attributes["x-show"] = "show"
        attributes["x-transition.opacity.duration.500ms"] = ""
        attributes["class"] = when (type) {
            "success" -> "success-message animate-fade-in relative"
            "error" -> "error-message animate-fade-in relative"
            else -> "$type-message relative"
        }

        span {
            attributes["class"] = "message-content"
            +message
        }

        button {
            attributes["class"] = "absolute top-2 right-2 text-sm opacity-75 hover:opacity-100"
            attributes["@click"] = "show = false"
            attributes["type"] = "button"
            attributes["aria-label"] = "Dismiss message"
            +"×"
        }
    }
}

object ResponseComponents {
    fun success(message: String, redirectUrl: String? = null, redirectDelay: Int = 500) = createHTML().div {
        messageComponent(message, "success")
        redirectUrl?.let {
            unsafe {
                +"""
                    <script>
                        setTimeout(function() {
                            window.location.href = '$it';
                        }, $redirectDelay);
                    </script>
                    """.trimIndent()
            }
        }
    }

    fun error(message: String) = createHTML().div {
        messageComponent(message, "error")
    }
}
