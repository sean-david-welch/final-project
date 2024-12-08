package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun DIV.insightContentComponent(content: String) {
    div(classes = "spreadsheet-wrapper") {
        content.split("###").filter { it.isNotBlank() }.forEach { section ->
            val trimmedSection = section.trim()
            when {
                trimmedSection.startsWith("Summary") -> {
                    h4(classes = "section-title") {
                        +"Summary of Potential Savings"
                    }
                    formatInsightContent(trimmedSection.substringAfter("Summary"))
                }
                trimmedSection.contains("Total Potential Savings") -> {
                    h4(classes = "section-title") {
                        +"Total Potential Savings"
                    }
                    formatInsightContent(trimmedSection.substringAfter("Total Potential Savings"))
                }
                else -> {
                    val parts = trimmedSection.split(":", limit = 2)
                    if (parts.size == 2) {
                        h4(classes = "section-title") {
                            +parts[0].trim()
                        }
                        formatInsightContent(parts[1])
                    } else {
                        formatInsightContent(trimmedSection)
                    }
                }
            }
        }
    }
}

private fun DIV.formatInsightContent(content: String) {
    div(classes = "spreadsheet-section") {
        content.split("<br>").filter { it.isNotBlank() }.forEach { line ->
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("**Benefits") -> {
                    div(classes = "insight-benefits") {
                        +trimmedLine.removePrefix("**Benefits:**").trim()
                    }
                }
                trimmedLine.startsWith("-") -> {
                    div(classes = "insight-item") {
                        span(classes = "bullet-icon") { +"•" }
                        span { +trimmedLine.removePrefix("-").trim() }
                    }
                }
                else -> {
                    p(classes = "insight-text") {
                        +trimmedLine
                    }
                }
            }
        }
    }
}

object AIInsightComponents {
    fun formatInsight(content: String) = createHTML().div {
        // Success message using existing component
        messageComponent("AI insight generated successfully", "success")

        // Insight content
        div {
            attributes["x-data"] = "{show: true}"
            attributes["x-show"] = "show"
            attributes["x-transition.opacity.duration.500ms"] = ""
            attributes["class"] = "form-container mt-4"

            insightContentComponent(content)
        }
    }
}