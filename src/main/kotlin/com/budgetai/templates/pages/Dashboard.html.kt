package com.budgetai.templates.pages

import kotlinx.html.*
import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.templates.components.Navbar
import com.budgetai.templates.components.Footer
import com.budgetai.templates.components.NavItem
import com.budgetai.templates.components.SimpleFooterLink

fun DashboardTemplate(title: String, contentFn: DIV.() -> Unit): String {
    return BaseTemplate {
        Navbar(
            brandName = "BudgetAI",
            navItems = listOf(
                NavItem("Dashboard", "/", true),
                NavItem("Reports", "/reports"),
                NavItem("Settings", "/settings")
            )
        )

        main {
            classes = setOf("dashboard-layout")
            div {
                classes = setOf("dashboard-container")
                div {
                    classes = setOf("page-header")
                    h1 {
                        classes = setOf("page-title")
                        +title
                    }
                }
                div {
                    classes = setOf("content-wrapper")
                    contentFn()
                }
            }
        }

        Footer(
            links = listOf(
                SimpleFooterLink("Privacy", "/privacy"),
                SimpleFooterLink("Terms", "/terms")
            )
        )
    }
}

fun createDashboardPage(): String {
    return DashboardTemplate("Dashboard Overview") {
        div {
            classes = setOf("stats-grid")
            repeat(3) { index ->
                div {
                    classes = setOf("stat-card")
                    div {
                        classes = setOf("stat-label")
                        +when (index) {
                            0 -> "Total Revenue"
                            1 -> "Active Users"
                            else -> "Growth Rate"
                        }
                    }
                    div {
                        classes = setOf("stat-value")
                        +when (index) {
                            0 -> "$24,500"
                            1 -> "2,345"
                            else -> "+12.3%"
                        }
                    }
                }
            }
        }
        div {
            classes = setOf("activity-section")
            h2 {
                classes = setOf("activity-title")
                +"Recent Activity"
            }
            div {
                classes = setOf("activity-list")
                repeat(3) {
                    div {
                        classes = setOf("activity-item")
                        div {
                            classes = setOf("activity-text")
                            +"User completed action ${it + 1}"
                        }
                        div {
                            classes = setOf("activity-time")
                            +"2 minutes ago"
                        }
                    }
                }
            }
        }
    }
}