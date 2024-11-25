package com.budgetai.templates.components

import kotlinx.html.*

data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

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

fun FlowContent.Navbar(brandName: String = "Your Brand", navItems: List<NavItem> = listOf()) {
    nav(classes = "navbar") {
        div(classes = "navbar-container navbar-content") {
            div(classes = "navbar-brand-container") {
                span(classes = "navbar-brand-text") { +brandName }
            }
            div(classes = "navbar-desktop-menu navbar-menu-container") {
                ul(classes = "navbar-menu-list") {
                    renderNavItems(navItems)
                }
            }
        }
    }
}