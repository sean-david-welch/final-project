package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.SpendingSummaryCard(context: BaseTemplateContext) {
    if (context.auth.isAuthenticated) {
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Spending Summary" }
                div(classes = "report-actions") {
                    a(classes = "action-button", href = "/api/reports/spending-summary") {
                        attributes["download"] = "spending-summary.csv"
                        style = "text-decoration: none; display: inline-block;"
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
    if (context.auth.isAuthenticated) {
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"AI Insights" }
                div(classes = "report-actions") {
                    a(href = "/reports/insights") {
                        button(classes = "action-button") { +"Consult AI assistant" }
                    }
                }
            }
            div(classes = "report-content") {
                div(classes = "chart-placeholder")
            }
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
