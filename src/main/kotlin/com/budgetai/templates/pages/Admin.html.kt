package com.budgetai.templates.pages

import com.budgetai.models.BudgetDTO
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

fun createAdminPage(context: BaseTemplateContext, users: List<UserDTO>, budgets: List<BudgetDTO>, categories: List<CategoryDTO>) =
    AdminTemplate("Admin Dashboard", context) {
        // Stats Grid
        div(classes = "stats-grid") {
            val stats = listOf(
                Pair("Total Users", users.count().toString()), Pair("Total Budgets", budgets.count().toString()),
                Pair("Total Categories", categories.count().toString())
            )

            stats.forEach { (label, value) ->
                div(classes = "stat-card") {
                    div(classes = "stat-content") {
                        div {
                            p(classes = "stat-info") { +label }
                            p(classes = "stat-value") { +value }
                        }
                    }
                }
            }
        }

        // Quick Actions Card
        div(classes = "status-card") {
            h2(classes = "status-title") { +"Quick Actions" }
            div(classes = "quick-actions") {
                listOf(
                    Pair("User Management", "/admin/user-management"), Pair("Manage Reports", "/admin/budget-management")
                ).forEach { (label, href) ->
                    a(href = href, classes = "action-button") {
                        +label
                    }
                }
            }
        }
    }

fun createUserPage(context: BaseTemplateContext, users: List<UserDTO>) = AdminTemplate("User Management", context) {
    div(classes = "management-container") {
        // Header with total count
        div(classes = "management-header") {
            h2(classes = "management-title") { +"Users (${users.count()})" }
        }

        // User table
        div(classes = "table-container") {
            table(classes = "data-table") {
                thead {
                    tr {
                        th { +"Name" }
                        th { +"Email" }
                        th { +"Role" }
                        th { +"Actions" }
                    }
                }
                tbody {
                    users.forEach { user ->
                        tr {
                            td(classes = "table-cell") { +user.name }
                            td(classes = "table-cell") { +user.email }
                            td(classes = "table-cell") { +user.role }
                            td(classes = "table-actions") {
                                button(classes = "edit-button") {
                                    +"Edit"
                                }
                                button(classes = "delete-button") {
                                    +"Delete"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun createBudgetManagementPage(context: BaseTemplateContext, budgets: List<BudgetDTO>) = AdminTemplate("Budget Management", context) {
    div(classes = "management-container") {
        // Header with total count and add button
        div(classes = "management-header") {
            h2(classes = "management-title") { +"Budgets (${budgets.count()})" }
        }

        // Budget table
        div(classes = "table-container") {
            table(classes = "data-table") {
                thead {
                    tr {
                        th { +"Name" }
                        th { +"User ID" }
                        th { +"Description" }
                        th { +"Total Income" }
                        th { +"Total Expenses" }
                        th { +"Actions" }
                    }
                }
                tbody {
                    budgets.forEach { budget ->
                        tr {
                            td(classes = "table-cell") { +budget.name }
                            td(classes = "table-cell") { +budget.userId.toString() }
                            if (budget.description != null) {
                                td(classes = "table-cell description") { +budget.description }
                            }
                            td(classes = "table-cell money") { +"$${budget.totalIncome}" }
                            td(classes = "table-cell money") { +"$${budget.totalExpenses}" }
                            td(classes = "table-actions") {
                                button(classes = "edit-button") {
                                    +"Edit"
                                }
                                button(classes = "delete-button") {
                                    +"Delete"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}