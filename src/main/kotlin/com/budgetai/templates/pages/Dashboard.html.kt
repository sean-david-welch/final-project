package com.budgetai.templates.pages

import kotlinx.html.*
import com.budgetai.templates.layout.*

fun DashboardTemplate(title: String, contentFn: DIV.() -> Unit) = BaseTemplate {
    Navbar(
        brandName = "BudgetAI", navItems = listOf(
            NavItem("Dashboard", "/", true), NavItem("Reports", "/reports"), NavItem("Settings", "/settings")
        )
    )

    main {
        div(classes = "page-header") {
            h1(classes = "page-title") { +title }
        }
        div(classes = "content-section") {
            contentFn()
        }
    }

    Footer(
        links = listOf(
            SimpleFooterLink("Privacy", "/privacy"), SimpleFooterLink("Terms", "/terms")
        )
    )
}

fun createDashboardPage() = DashboardTemplate("Dashboard Overview") {
    // Stats Section
    div(classes = "grid-stats") {
        repeat(3) { index ->
            div(classes = "card-base") {
                div(classes = "stat-label") {
                    +when (index) {
                        0 -> "Total Revenue"
                        1 -> "Active Users"
                        else -> "Growth Rate"
                    }
                }
                div(classes = "stat-value") {
                    +when (index) {
                        0 -> "$24,500"
                        1 -> "2,345"
                        else -> "+12.3%"
                    }
                }
            }
        }
    }

    // Activity Section
    div(classes = "activity-section") {
        h2(classes = "activity-title") { +"Recent Activity" }
        div(classes = "list-container") {
            repeat(3) {
                div(classes = "list-item") {
                    div(classes = "activity-text") { +"User completed action ${it + 1}" }
                    div(classes = "activity-time") { +"2 minutes ago" }
                }
            }
        }
    }
}

// Helper functions to make the template more reusable
fun DIV.statCard(label: String, value: String) {
    div(classes = "card-base") {
        div(classes = "stat-label") { +label }
        div(classes = "stat-value") { +value }
    }
}

fun DIV.activityItem(text: String, time: String) {
    div(classes = "list-item") {
        div(classes = "activity-text") { +text }
        div(classes = "activity-time") { +time }
    }
}