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
                    Pair("User Management", "/admin/user-management"), Pair("Manage Budgets", "/admin/budget-management")
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
            if (context.auth.isAdmin) {
                div(classes = "admin-access-section") {
                    a(href = "/admin", classes = "admin-link-button") {
                        +"Admin Panel"
                    }
                }
            }
        }

        div {
            attributes["id"] = "response-message"
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
                            attributes["id"] = "user-row-${user.id}"
                            td(classes = "table-cell") { +user.name }
                            td(classes = "table-cell") { +user.email }
                            td(classes = "table-cell") {
                                attributes["id"] = "role-cell-${user.id}"
                                +user.role
                            }
                            td(classes = "table-actions") {
                                select(classes = "role-select") {
                                    attributes["hx-put"] = "/api/users/${user.id}/role"
                                    attributes["hx-target"] = "#response-message"
                                    attributes["hx-swap"] = "innerHTML"
                                    attributes["hx-trigger"] = "change"
                                    attributes["name"] = "role"
                                    attributes["value"] = user.role
                                    attributes["hx-on::after-request"] = "if(event.detail.successful) this.closest('tr').querySelector('#role-cell-${user.id}').innerHTML = this.value"
                                    val roles = if (user.role == "ADMIN") {
                                        listOf("ADMIN" to "Admin", "USER" to "User")
                                    } else {
                                        listOf("USER" to "User", "ADMIN" to "Admin")
                                    }
                                    roles.forEach { (value, label) ->
                                        option {
                                            attributes["value"] = value
                                            if (value == user.role) {
                                                attributes["selected"] = "selected"
                                            }
                                            +label
                                        }
                                    }
                                }
                                button(classes = "delete-button") {
                                    attributes["hx-delete"] = "/api/users/${user.id}"
                                    attributes["hx-target"] = "#response-message"
                                    attributes["hx-swap"] = "innerHTML"
                                    attributes["hx-confirm"] = "Are you sure you want to delete this user?"
                                    attributes["hx-on::after-request"] = "if(event.detail.successful) document.getElementById('user-row-${user.id}').remove()"
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
            if (context.auth.isAdmin) {
                div(classes = "admin-access-section") {
                    a(href = "/admin", classes = "admin-link-button") {
                        +"Admin Panel"
                    }
                }
            }
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
                            td(classes = "table-cell description") { +(budget.description ?: "-") }
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