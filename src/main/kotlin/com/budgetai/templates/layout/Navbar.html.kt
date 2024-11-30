package com.budgetai.templates.layout

import kotlinx.html.*

// Navitem data class
data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

// helper function to render navitems
private fun UL.renderNavItems(items: List<NavItem>) {
    items.forEach { item ->
        li {
            a(href = item.href) {
                classes = if (item.isActive) setOf("nav-item-active") else setOf("nav-item-inactive")
                +item.text
            }
        }
    }
}

// default nav items for all pages
val navItems = listOf(
    NavItem("Dashboard", "/", true), NavItem("Reports", "/reports"), NavItem("Settings", "/settings")
)

// main navbar template
fun FlowContent.Navbar(navItems: List<NavItem>? = null) {
    nav(classes = "navbar") {
        div(classes = "navbar-container navbar-content") {
            div(classes = "navbar-brand-container") {
                span(classes = "navbar-brand-text") { +"BudgetAI" }
            }
            div(classes = "navbar-desktop-menu navbar-menu-container") {
                ul(classes = "navbar-menu-list") {
                    if (navItems != null) {
                        renderNavItems(navItems)
                    }
                }
            }
        }
    }
}