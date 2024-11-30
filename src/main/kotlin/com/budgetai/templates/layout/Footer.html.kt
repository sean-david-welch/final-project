package com.budgetai.templates.layout

import kotlinx.html.*

data class FooterLink(val text: String, val href: String)

val links = listOf(
    FooterLink("Privacy", "/privacy"), FooterLink("Terms", "/terms")
)

fun FlowContent.Footer() {
    footer(classes = "footer") {
        div(classes = "footer-container") {
            div(classes = "footer-copyright") { +"© ${java.time.Year.now().value} BudgetAI" }
            div(classes = "footer-links-container") {
                links.forEach { link ->
                    a(href = link.href, classes = "footer-link") { +link.text }
                }
            }
        }
    }
}