package com.budgetai.templates.pages

import com.budgetai.models.BudgetDTO
import com.budgetai.models.Categories
import com.budgetai.models.CategoryDTO
import com.budgetai.models.UserDTO
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

fun createAdminPage(context: BaseTemplateContext, users: List<UserDTO>, budgets: List<BudgetDTO>, categories: List<CategoryDTO>) = AdminTemplate("Admin Dashboard", context) {
    // Stats Grid
    div(classes = "stats-grid") {
        val stats = listOf(
            Pair("Total Users", users.count().toString()), Pair("Total Budgets", budgets.count().toString()),
            Pair("Total Categories", categories.count().toString())
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