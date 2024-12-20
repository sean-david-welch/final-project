package com.budgetai.templates.layout

import com.budgetai.templates.components.logoutButton
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

data class FooterLink(val text: String, val href: String)

val links = listOf(
    FooterLink("Dashboard", "/dashboard"), FooterLink("Reports", "/reports"), FooterLink("Settings", "/settings")
)

fun FlowContent.Footer(context: BaseTemplateContext) {
    footer(classes = "footer") {
        div(classes = "footer-container") {
            a(classes = "navbar-brand-container", href = "/") {
                img(src = "/static/images/logo.png", alt = "logo-image", classes = "nav-logo-lg")
                span(classes = "navbar-brand-text") { +"BudgetAI" }
            }
            div(classes = "footer-copyright") { +"© ${java.time.Year.now().value} BudgetAI" }
            div(classes = "footer-links-container") {
                if (context.auth.isAuthenticated) {
                    links.forEach { link ->
                        a(href = link.href, classes = "footer-link") { +link.text }
                    }
                    logoutButton()
                } else {
                    a(href = "/auth", classes = "nav-item-inactive login") { +"Login" }
                }
            }
        }
    }
}