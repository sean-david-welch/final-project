package com.budgetai.templates.layout

import com.budgetai.utils.BaseTemplateContext
import com.budgetai.templates.components.logoutButton
import kotlinx.html.*

// Navitem data class
data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

// default nav items for all pages
val defaultNavItems = listOf(
    NavItem("Dashboard", "/dashboard", true), NavItem("Reports", "/reports"), NavItem("Settings", "/settings")
)

// main navbar template
fun FlowContent.Navbar(context: BaseTemplateContext) {
    nav(classes = "navbar") {
        div(classes = "navbar-container navbar-content") {
            a(classes = "navbar-brand-container", href = "/") {
                img(src = "/static/images/logo.png", alt = "logo-image", classes = "nav-logo")
                span(classes = "navbar-brand-text") { +"BudgetAI" }
            }
            div(classes = "navbar-desktop-menu navbar-menu-container") {
                ul(classes = "navbar-menu-list") {
                    if (context.auth.isAuthenticated) {
                        // Show nav items based on authentication status
                        defaultNavItems.forEach { item ->
                            li {
                                a(href = item.href) {
                                    classes = if (item.isActive) setOf("nav-item-active") else setOf("nav-item-inactive")
                                    +item.text
                                }
                            }
                        }
                        li { logoutButton() }
                    } else {
                        a(href = "/auth", classes = "nav-item-inactive") {
                            +"Login"
                        }
                    }
                }
            }
        }
    }
}