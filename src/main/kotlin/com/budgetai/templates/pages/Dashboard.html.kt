package com.budgetai.templates.pages

import kotlinx.html.*
import com.budgetai.templates.layout.*

fun DashboardTemplate(title: String, contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createDashboardPage() = DashboardTemplate("Dashboard Overview") {
    div(classes = "stats-grid") {
        repeat(3) { index ->
            div(classes = "stat-card") {
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

    div(classes = "activity-section") {
        h2(classes = "activity-title") { +"Recent Activity" }
        div(classes = "activity-list") {
            repeat(3) {
                div(classes = "activity-item") {
                    div(classes = "activity-text") { +"User completed action ${it + 1}" }
                    div(classes = "activity-time") { +"2 minutes ago" }
                }
            }
        }
    }
}