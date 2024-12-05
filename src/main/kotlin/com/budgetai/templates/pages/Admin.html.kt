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
            Triple("Total Users", "12,453", "users-icon"), Triple("Total Budgets", "12,453", "users-icon"),
            Triple("Total Categories", "12,453", "users-icon")
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

    // Quick Actions Card
    div(classes = "status-card") {
        h2(classes = "status-title") { +"Quick Actions" }
        div(classes = "quick-actions") {
            listOf(
                "User Management", "Manage Reports",
            ).forEach { action ->
                button(classes = "action-button") {
                    +action
                }
            }
        }
    }
}