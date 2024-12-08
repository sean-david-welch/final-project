package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.SpendingSummaryCard(context: BaseTemplateContext) {
    if (context.auth.isAuthenticated) {
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Spending Summary" }
                div(classes = "report-actions") {
                    button(classes = "action-button") {
                        attributes["hx-get"] = "/api/reports/spending-summary"
                        attributes["hx-trigger"] = "click"
                        attributes["hx-swap"] = "none"
                        attributes["hx-headers"] = "{\"Accept\": \"text/csv\"}"
                        attributes["download"] = "spending-summary.csv"
                        +"Download CSV"
                    }
                }
            }
            div(classes = "report-content") {
                div(classes = "chart-placeholder")
            }
        }
    }
}

fun FlowContent.BudgetAnalysisCard(context: BaseTemplateContext) {
    DialogComponent(context) {
        h2(classes = "heading-large") {
            +"Savings Goals"
        }
        div(classes = "text-base mt-4") {
            +"Here's a list of the savings goals you've created"
        }
    }
    div(classes = "report-card") {
        div(classes = "report-header") {
            h3(classes = "report-title") { +"AI Insights" }
            div(classes = "report-actions") {
                button(classes = "action-button") {
                    attributes["onclick"] = "document.getElementById('modal-dialog').showModal()"
                    +"Consult AI advisor"
                }
            }
        }
        div(classes = "report-content") {
            div(classes = "chart-placeholder")
        }
    }
}

fun FlowContent.CategoryBreakdownCard(context: BaseTemplateContext) {
    if (context.auth.isAuthenticated) {
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Category Breakdown" }
                div(classes = "report-actions") {
                    div(classes = "report-actions") {
                        a(href = "/reports/category-breakdown") {
                            button(classes = "action-button") { +"View Details" }
                        }
                    }
                }
            }
            div(classes = "report-content") {
                div(classes = "chart-placeholder")
            }
        }
    }
}

fun FlowContent.SavingsTrackingCard(context: BaseTemplateContext) {
    if (context.auth.isAuthenticated) {
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Savings Tracking" }
                div(classes = "report-actions") {
                    a(href = "/reports/savings-tracking") {
                        button(classes = "action-button") { +"View Goals" }
                    }
                }
            }
            div(classes = "report-content") {
                div(classes = "chart-placeholder")
            }
        }
    }
}
