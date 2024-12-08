package com.budgetai.templates.components

import kotlinx.html.*

fun FlowContent.SpendingSummaryCard() {
    div(classes = "report-card") {
        div(classes = "report-header") {
            h3(classes = "report-title") { +"Spending Summary" }
            div(classes = "report-actions") {
                button(classes = "action-button") { +"Download PDF" }
            }
        }
        div(classes = "report-content") {
            div(classes = "chart-placeholder")
        }
    }
}

fun FlowContent.BudgetAnalysisCard() {
    div(classes = "report-card") {
        div(classes = "report-header") {
            h3(classes = "report-title") { +"AI Insights" }
            div(classes = "report-actions") {
                button(classes = "action-button") { +"Consult AI advisor" }
            }
        }
        div(classes = "report-content") {
            div(classes = "chart-placeholder")
        }
    }
}

fun FlowContent.CategoryBreakdownCard() {
    div(classes = "report-card") {
        div(classes = "report-header") {
            h3(classes = "report-title") { +"Category Breakdown" }
            div(classes = "report-actions") {
                button(classes = "action-button") { +"View Details" }
            }
        }
        div(classes = "report-content") {
            div(classes = "chart-placeholder")
        }
    }
}

fun FlowContent.SavingsTrackingCard() {
    div(classes = "report-card") {
        div(classes = "report-header") {
            h3(classes = "report-title") { +"Savings Tracking" }
            div(classes = "report-actions") {
                button(classes = "action-button") { +"Set Goals" }
            }
        }
        div(classes = "report-content") {
            div(classes = "chart-placeholder")
        }
    }
}
