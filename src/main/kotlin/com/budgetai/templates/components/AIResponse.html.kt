package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

object AIInsightComponents {
    fun formatInsight(content: String) = createHTML().div {
        div(classes = "space-y-4") {
            // Success Message
            div {
                attributes["x-data"] = "{show: true}"
                attributes["x-show"] = "show"
                attributes["x-transition.opacity.duration.500ms"] = ""
                attributes["class"] = "success-message animate-fade-in relative"

                span {
                    attributes["class"] = "message-content"
                    +"AI insight generated successfully"
                }

                button {
                    attributes["class"] = "absolute w-10 right-2 text-sm opacity-75 hover:opacity-100"
                    attributes["onclick"] = "this.parentElement.remove()"
                    attributes["type"] = "button"
                    attributes["aria-label"] = "Dismiss message"
                    +"×"
                }
            }

            // Insight Content
            div(classes = "bg-secondary-800 rounded-lg p-6 shadow-lg space-y-4 text-white mt-4") {
                h3(classes = "text-xl font-semibold mb-4") {
                    +"Analysis Results"
                }

                div(classes = "ai-insight-content prose prose-invert max-w-none") {
                    content.split("###").filter { it.isNotBlank() }.forEach { section ->
                        val trimmedSection = section.trim()
                        when {
                            trimmedSection.startsWith("Summary") -> {
                                h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                    +"Summary of Potential Savings"
                                }
                                formatSection(trimmedSection.substringAfter("Summary"))
                            }
                            trimmedSection.contains("Total Potential Savings") -> {
                                h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                    +"Total Potential Savings"
                                }
                                formatSection(trimmedSection.substringAfter("Total Potential Savings"))
                            }
                            else -> {
                                val parts = trimmedSection.split(":", limit = 2)
                                if (parts.size == 2) {
                                    h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                        +parts[0].trim()
                                    }
                                    formatSection(parts[1])
                                } else {
                                    formatSection(trimmedSection)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun DIV.formatSection(content: String) {
        div(classes = "space-y-2") {
            content.split("<br>").filter { it.isNotBlank() }.forEach { line ->
                val trimmedLine = line.trim()
                when {
                    trimmedLine.startsWith("**Benefits") -> {
                        div(classes = "pl-4 text-primary-100 italic") {
                            +trimmedLine.removePrefix("**Benefits:**").trim()
                        }
                    }
                    trimmedLine.startsWith("-") -> {
                        div(classes = "pl-4 flex items-start gap-2") {
                            i(classes = "ri-arrow-right-line mt-1 text-primary-300")
                            span { +trimmedLine.removePrefix("-").trim() }
                        }
                    }
                    else -> {
                        p(classes = "text-gray-200") {
                            +trimmedLine
                        }
                    }
                }
            }
        }
    }
}