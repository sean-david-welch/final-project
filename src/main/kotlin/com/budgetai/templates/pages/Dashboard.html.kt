package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun DashboardTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createDashboardPage(context: BaseTemplateContext) = DashboardTemplate("Dashboard Overview", context) {
    if (context.auth.isAuthenticated) { h1 { +"Youre logged in!!!" } }
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