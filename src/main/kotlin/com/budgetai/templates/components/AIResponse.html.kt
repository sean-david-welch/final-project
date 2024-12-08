package com.budgetai.templates.components

import kotlinx.html.*

fun FlowContent.AIInsightDisplay(content: String) {
    div(classes = "space-y-4") {
        div(classes = "alert alert-success") {
            role = "alert"
            div(classes = "flex items-center gap-2") {
                i(classes = "ri-checkbox-circle-line text-lg")
                span { +"AI insight generated successfully" }
            }
        }

        div(classes = "bg-secondary-800 rounded-lg p-6 shadow-lg space-y-4 text-white") {
            h3(classes = "text-xl font-semibold mb-4") {
                +"Analysis Results"
            }

            div(classes = "ai-insight-content prose prose-invert max-w-none") {
                // Format different sections
                content.split("###").filter { it.isNotBlank() }.forEach { section ->
                    val trimmedSection = section.trim()
                    when {
                        trimmedSection.startsWith("Summary") -> {
                            h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                +"Summary of Potential Savings"
                            }
                            processSectionContent(trimmedSection.substringAfter("Summary"))
                        }
                        trimmedSection.contains("Total Potential Savings") -> {
                            h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                +"Total Potential Savings"
                            }
                            processSectionContent(trimmedSection.substringAfter("Total Potential Savings"))
                        }
                        else -> {
                            // Regular sections with numbered items
                            val parts = trimmedSection.split(":", limit = 2)
                            if (parts.size == 2) {
                                h4(classes = "text-lg font-semibold text-primary-300 mt-6 mb-3") {
                                    +parts[0].trim()
                                }
                                processSectionContent(parts[1])
                            } else {
                                processSectionContent(trimmedSection)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.processSectionContent(content: String) {
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