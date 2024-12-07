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
                    "User Management", "Manage Reports",
                ).forEach { action ->
                    button(classes = "action-button") {
                        +action
                    }
                }
            }
        }
    }

fun createUserPage(context: BaseTemplateContext, users: List<UserDTO>) = AdminTemplate("User Management", context) {
    div(classes = "user-management-container") {
        // Header with total count
        div(classes = "user-header") {
            h2(classes = "user-title") { +"Users (${users.count()})" }
            button(classes = "add-user-button") {
                +"Add New User"
            }
        }

        // User table
        div(classes = "user-table-container") {
            table(classes = "user-table") {
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
                            td(classes = "user-cell") { +user.name }
                            td(classes = "user-cell") { +user.email }
                            td(classes = "user-cell") { +user.role }
                            td(classes = "user-actions") {
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
    div(classes = "budget-management-container") {
        // Header with total count and add button
        div(classes = "budget-header") {
            h2(classes = "budget-title") { +"Budgets (${budgets.count()})" }
            button(classes = "add-budget-button") {
                +"Create New Budget"
            }
        }

        // Budget table
        div(classes = "budget-table-container") {
            table(classes = "budget-table") {
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
                            td(classes = "budget-cell") { +budget.name }
                            td(classes = "budget-cell") { +budget.userId.toString() }
                            if (budget.description != null) {
                                td(classes = "budget-cell description") { +budget.description }
                            }
                            td(classes = "budget-cell money") { +"$${budget.totalIncome}" }
                            td(classes = "budget-cell money") { +"$${budget.totalExpenses}" }
                            td(classes = "budget-actions") {
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