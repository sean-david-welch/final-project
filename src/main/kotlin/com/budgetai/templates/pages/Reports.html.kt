package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun ReportsTemplate(title: String, context: BaseTemplateContext ,contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "reports-layout") {
        div(classes = "reports-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createReportsPage(context: BaseTemplateContext) = ReportsTemplate("Financial Reports & Analytics", context) {
    // Reports Overview Section
    div(classes = "reports-overview") {
        div(classes = "overview-header") {
            h2(classes = "overview-title") { +"Your Financial Overview" }
            p(classes = "overview-description") {
                +"Comprehensive insights into your spending patterns and financial health."
            }
        }
    }

    // Reports Grid
    div(classes = "reports-grid") {
        // Spending Summary Card
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Spending Summary" }
                div(classes = "report-actions") {
                    button(classes = "action-button") { +"Download PDF" }
                }
            }
            div(classes = "report-content") {
                // Placeholder for chart/visualization
                div(classes = "chart-placeholder")
            }
        }

        // Budget Analysis Card
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Ai Insights" }
                div(classes = "report-actions") {
                    button(classes = "action-button") { +"Consult AI advisor" }
                }
            }
            div(classes = "report-content") {
                div(classes = "chart-placeholder")
            }
        }

        // Category Breakdown Card
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

        // Savings Tracking Card
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
}