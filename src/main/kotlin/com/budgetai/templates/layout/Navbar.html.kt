package com.budgetai.templates.layout

import kotlinx.html.*

// Navitem data class
data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

// default nav items for all pages
val navItems = listOf(
    NavItem("Dashboard", "/", true), NavItem("Reports", "/reports"), NavItem("Settings", "/settings")
)

// main navbar template
fun FlowContent.Navbar() {
    nav(classes = "navbar") {
        div(classes = "navbar-container navbar-content") {
            div(classes = "navbar-brand-container") {
                span(classes = "navbar-brand-text") { +"BudgetAI" }
            img(src = "/static/images/logo.png", alt = "logo-image", classes = "nav-logo")
            }
            div(classes = "navbar-desktop-menu navbar-menu-container") {
                ul(classes = "navbar-menu-list") {
                    navItems.forEach { item ->
                        li {
                            a(href = item.href) {
                                classes = if (item.isActive) setOf("nav-item-active") else setOf("nav-item-inactive")
                                +item.text
                            }
                        }
                    }
                }
            }
        }
    }
}