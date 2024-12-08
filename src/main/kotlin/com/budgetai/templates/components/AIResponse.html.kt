package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun DIV.insightContentComponent(content: String) {
    div(classes = "insight-container") {
        // Split content by double asterisks to separate sections
        content.split("**").filter { it.isNotBlank() }.forEach { section ->
            when {
                // Handle section titles/headers
                section.contains(":**") -> {
                    val parts = section.split(":**", limit = 2)
                    h4(classes = "insight-header") {
                        +parts[0].trim()
                    }
                    if (parts.size > 1) {
                        formatSectionContent(parts[1].trim())
                    }
                }
                // Handle regular content
                else -> formatSectionContent(section.trim())
            }
        }
    }
}

private fun DIV.formatSectionContent(content: String) {
    div(classes = "insight-section") {
        content.split("- ").filter { it.isNotBlank() }.forEach { item ->
            when {
                // Format numbered items (e.g., "1. Action:")
                item.matches(Regex("\\d+\\..*")) -> {
                    div(classes = "insight-item numbered") {
                        +item.trim()
                    }
                }
                // Format bullet points
                item.contains(":") -> {
                    div(classes = "insight-item with-label") {
                        val parts = item.split(":", limit = 2)
                        span(classes = "item-label") {
                            +"${parts[0].trim()}:"
                        }
                        if (parts.size > 1) {
                            span(classes = "item-content") {
                                +parts[1].trim()
                            }
                        }
                    }
                }
                // Regular text
                else -> {
                    p(classes = "insight-text") {
                        +item.trim()
                    }
                }
            }
        }
    }
}

object AIInsightComponents {
    fun formatInsight(content: String) = createHTML().div {
        div(classes = "insight-wrapper") {
            insightContentComponent(content)
        }
    }
}