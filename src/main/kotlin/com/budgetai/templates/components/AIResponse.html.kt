package com.budgetai.templates.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun DIV.insightContentComponent(content: String) {
    div(classes = "insight-container") {
        content.split("###").filter { it.isNotBlank() }.forEach { section ->
            val lines = section.trim().split("\n").filter { it.isNotBlank() }
            lines.forEachIndexed { index, line ->
                when {
                    index == 0 -> h4(classes = "insight-header") { +line.trim() }
                    line.startsWith("- ") -> div(classes = "insight-item") { +line.substring(2).trim() }
                    line.contains(":") -> {
                        val (label, value) = line.split(":", limit = 2)
                        div(classes = "insight-item with-label") {
                            span(classes = "item-label") { +label.trim() }
                            span(classes = "item-content") { +value.trim() }
                        }
                    }
                    else -> p(classes = "insight-text") { +line.trim() }
                }
            }
        }
    }
}

object AIInsightComponents {
    fun formatInsight(content: String) = createHTML().div {
        insightContentComponent(content)
    }
}