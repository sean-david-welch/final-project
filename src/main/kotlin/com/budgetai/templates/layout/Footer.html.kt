package com.budgetai.templates.layout

import kotlinx.html.*

data class SimpleFooterLink(val text: String, val href: String)

fun FlowContent.Footer(links: List<SimpleFooterLink> = listOf()) {
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