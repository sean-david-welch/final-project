package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun AdminTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "admin-dashboard") {
        div(classes = "admin-container") {
            h1(classes = "admin-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createAdminPage(context: BaseTemplateContext) = AdminTemplate("Admin Dashboard", context) {
    // Stats Grid
    div(classes = "stats-grid") {
        val stats = listOf(
            Triple("Total Users", "12,453", "users-icon"),
            Triple("System Alerts", "7", "alert-icon"),
            Triple("Active Sessions", "2,145", "activity-icon"),
            Triple("System Health", "98.2%", "settings-icon")
        )

        stats.forEach { (label, value, iconClass) ->
            div(classes = "stat-card") {
                div(classes = "stat-content") {
                    div {
                        p(classes = "stat-info") { +label }
                        p(classes = "stat-value") { +value }
                    }
                    div(classes = "stat-icon $iconClass") {}
                }
            }
        }
    }

    // Recent Actions Section
    div(classes = "actions-section") {
        div(classes = "actions-card") {
            h2(classes = "actions-title") { +"Recent Admin Actions" }
            div(classes = "actions-list") {
                val actions = listOf(
                    Triple("admin@example.com", "Updated user permissions", "5 minutes ago"),
                    Triple("system", "Backup completed", "1 hour ago"),
                    Triple("moderator@example.com", "Flagged content removed", "2 hours ago")
                )

                actions.forEach { (user, action, time) ->
                    div(classes = "action-item") {
                        div(classes = "action-content") {
                            div {
                                p(classes = "action-details") { +action }
                                p(classes = "action-user") { +"by $user" }
                            }
                            span(classes = "action-time") { +time }
                        }
                    }
                }
            }
        }
    }

    // Status Grid
    div(classes = "status-grid") {
        // System Status Card
        div(classes = "status-card") {
            h2(classes = "status-title") { +"System Status" }
            div(classes = "status-list") {
                val statusItems = listOf(
                    Pair("CPU Usage", "42%"),
                    Pair("Memory Usage", "3.2 GB"),
                    Pair("Storage", "67%")
                )

                statusItems.forEach { (label, value) ->
                    div(classes = "status-item") {
                        span(classes = "status-label") { +label }
                        span(classes = "status-value") { +value }
                    }
                }
            }
        }

        // Quick Actions Card
        div(classes = "status-card") {
            h2(classes = "status-title") { +"Quick Actions" }
            div(classes = "quick-actions") {
                listOf(
                    "User Management",
                    "System Settings",
                    "Audit Logs",
                    "Backup"
                ).forEach { action ->
                    button(classes = "action-button") {
                        +action
                    }
                }
            }
        }
    }
}