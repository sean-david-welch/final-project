package com.budgetai.templates.pages

import com.budgetai.models.BudgetDTO
import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.CategoryDTO
import com.budgetai.models.SavingsGoalDTO
import com.budgetai.templates.components.*
import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun ReportsTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "reports-layout") {
        div(classes = "reports-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createReportsPage(
    context: BaseTemplateContext, budgets: List<BudgetDTO>, budgetItems: List<BudgetItemDTO>, categories: List<CategoryDTO>
) = ReportsTemplate("Financial Reports & Analytics", context) {
    // Reports Overview Section
    div(classes = "reports-overview") {
        div(classes = "overview-header") {
            h2(classes = "overview-title") { +"Your Financial Overview" }
            p(classes = "overview-description") {
                +"Comprehensive insights into your spending patterns and financial health."
            }
        }
    }

    // Stats Grid
    div(classes = "stats-grid") {
        val stats = listOf(
            Pair("Total Budget Items", budgetItems.count().toString()), Pair("Total Budgets", budgets.count().toString()),
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

    // Reports Grid
    div(classes = "reports-grid") {
        SpendingSummaryCard(context)
        BudgetAnalysisCard(context)
        CategoryBreakdownCard(context)
        SavingsTrackingCard(context)
    }
}

fun createCategoryManagementPage(context: BaseTemplateContext, categories: List<CategoryDTO>) =
    AdminTemplate("Category Management", context) {
        div(classes = "management-container") {
            // Header with total count and add button
            div(classes = "management-header") {
                h2(classes = "management-title") { +"Categories (${categories.count()})" }
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

            // Category table
            div(classes = "table-container") {
                table(classes = "data-table") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Type" }
                            th { +"Description" }
                        }
                    }
                    tbody {
                        categories.forEach { category ->
                            tr {
                                attributes["id"] = "category-row-${category.id}"
                                td(classes = "table-cell") { +category.name }
                                td(classes = "table-cell description") { +(category.description ?: "-") }
                                td(classes = "table-actions") {
                                    select(classes = "role-select") {
                                        attributes["hx-put"] = "/api/categories/${category.id}/type"
                                        attributes["hx-target"] = "#response-message"
                                        attributes["hx-swap"] = "innerHTML"
                                        attributes["hx-trigger"] = "change"
                                        attributes["name"] = "type"
                                        attributes["value"] = category.type.toString()
                                        attributes["hx-on::after-request"] = "if(event.detail.successful) this.closest('tr').querySelector('#type-cell-${category.id}').innerHTML = this.value"

                                        val categoryTypes = listOf(
                                            "EXPENSE" to "Expense", "INCOME" to "Income"
                                        )

                                        categoryTypes.forEach { (value, label) ->
                                            option {
                                                attributes["value"] = value
                                                if (value == category.type.toString()) {
                                                    attributes["selected"] = "selected"
                                                }
                                                +label
                                            }
                                        }
                                    }
                                    button(classes = "delete-button") {
                                        attributes["hx-delete"] = "/api/categories/${category.id}"
                                        attributes["hx-target"] = "#response-message"
                                        attributes["hx-swap"] = "innerHTML"
                                        attributes["hx-confirm"] = "Are you sure you want to delete this category?"
                                        attributes["hx-on::after-request"] = "if(event.detail.successful) document.getElementById('category-row-${category.id}').remove()"
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

fun createSavingsManagementPage(context: BaseTemplateContext, savings: List<SavingsGoalDTO>) =
    AdminTemplate("Savings Goal Management", context) {
        DialogComponent(context) {
            h2(classes = "heading-large") {
                +"Savings Goals"
            }
            div(classes = "text-base mt-4") {
                +"Create a savings goal below"
            }
            SavingsGoalForm(context)
        }

        div(classes = "management-container") {
            div(classes = "management-header") {
                h2(classes = "management-title") { +"Savings Goals (${savings.count()})" }
                button(classes = "action-button") {
                    attributes["onclick"] = "document.getElementById('modal-dialog').showModal()"
                    +"Set Goal"
                }
            }
            div {
                attributes["id"] = "response-message"
                attributes["hx-get"] = "/api/savings-goals/list"
                attributes["hx-trigger"] = "refreshGoals from:body"
            }

            div(classes = "table-container") {
                div(
                    attributes = mapOf(
                        "id" to "goals-table", "hx-get" to "/api/savings-goals/user/${context.auth.user?.id}",
                        "hx-trigger" to "refreshGoals from:body"
                    )
                ) {
                    SavingsGoalTable(savings)
                }
            }
        }
    }

fun FlowContent.SavingsGoalTable(goals: List<SavingsGoalDTO>) {
    table(classes = "data-table") {
        thead {
            tr {
                th { +"Name" }
                th { +"Description" }
                th { +"Target Amount" }
                th { +"Current Amount" }
                th { +"Target Date" }
                th { +"Actions" }
            }
        }
        tbody {
            goals.forEach { goal ->
                tr {
                    attributes["id"] = "goal-row-${goal.id}"
                    td(classes = "table-cell") { +goal.name }
                    td(classes = "table-cell description") { +(goal.description ?: "-") }
                    td(classes = "table-cell") { +"$${goal.targetAmount}" }
                    td(classes = "table-cell") { +"$${goal.currentAmount}" }
                    td(classes = "table-cell") { +(goal.targetDate ?: "-") }
                    td(classes = "table-actions") {
                        button(classes = "delete-button") {
                            attributes["hx-delete"] = "/api/savings-goals/${goal.id}"
                            attributes["hx-target"] = "#goals-table"
                            attributes["hx-swap"] = "innerHTML"
                            attributes["hx-confirm"] = "Are you sure you want to delete this goal?"
                            +"Delete"
                        }
                    }
                }
            }
        }
    }
}