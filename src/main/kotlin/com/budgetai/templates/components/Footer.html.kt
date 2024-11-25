package com.budgetai.templates.components

import kotlinx.html.*

data class SimpleFooterLink(
    val text: String, val href: String
)

fun FlowContent.Footer(links: List<SimpleFooterLink> = listOf()) {
    footer {
        classes = setOf("footer")
        div {
            classes = setOf("footer-container")
            div {
                classes = setOf("footer-copyright") +"© ${java.time.Year.now().value} BudgetAI"
            }
            div {
                classes = setOf("footer-links-container")
                links.forEach { link ->
                    a(href = link.href) {
                        classes = setOf("footer-link")
                        +link.text
                    }
                }
            }
        }
    }
}