package com.budgetai.templates.layout

import com.budgetai.utils.BaseTemplateContext
import com.budgetai.templates.components.logoutButton
import io.ktor.server.request.*
import kotlinx.html.*

// Navitem data class
data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

// default nav items for all pages
fun getNavItems(currentPath: String): List<NavItem> {
    return listOf(
        NavItem("Dashboard", "/dashboard", currentPath.startsWith("/dashboard")),
        NavItem("Reports", "/reports", currentPath.startsWith("/reports")),
        NavItem("Settings", "/settings", currentPath.startsWith("/settings"))
    )
}

// main navbar template
fun FlowContent.Navbar(context: BaseTemplateContext) {
    val currentPath = context.request.path()

    nav(classes = "navbar") {
        div(classes = "navbar-container navbar-content") {
            a(classes = "navbar-brand-container", href = "/") {
                img(src = "/static/images/logo.png", alt = "logo-image", classes = "nav-logo")
                span(classes = "navbar-brand-text") { +"BudgetAI" }
            }
            div(classes = "navbar-desktop-menu navbar-menu-container") {
                ul(classes = "navbar-menu-list") {
                    if (context.auth.isAuthenticated) {
                        getNavItems(currentPath).forEach { item ->
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