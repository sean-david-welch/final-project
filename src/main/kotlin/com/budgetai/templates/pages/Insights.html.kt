package com.budgetai.templates.pages


import com.budgetai.models.BudgetDTO
import com.budgetai.templates.components.*
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun AIInsightsTemplate(context: BaseTemplateContext, budgets: List<BudgetDTO>) = DashboardTemplate("AI Insights", context) {
    // Header Section
    div(classes = "management-header") {
        div {
            h2(classes = "overview-title") { +"AI Budget Analysis" }
            p(classes = "overview-description") {
                +"Get personalized insights and recommendations for your budgets"
            }
        }
        button(classes = "action-button") {
            attributes["onclick"] = "document.getElementById('modal-dialog').showModal()"
            +"New Analysis"
        }
    }

    // Main Content
    div(classes = "content-grid") {
        // Analysis Card
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Recent Insights" }
                div(classes = "report-actions") {
                    button(classes = "action-button") {
                        attributes["onclick"] = "document.getElementById('modal-dialog').showModal()"
                        +"Consult AI Advisor"
                    }
                }
            }
            div(classes = "report-content") {
                div(classes = "insights-list") {
                    // Placeholder for insights list
                    div(classes = "chart-placeholder") {
                        +"No insights available yet. Click 'New Analysis' to get started."
                    }
                }
            }
        }

        // Stats Card
        div(classes = "report-card") {
            div(classes = "report-header") {
                h3(classes = "report-title") { +"Analysis Statistics" }
            }
            div(classes = "report-content") {
                div(classes = "stats-grid") {
                    div(classes = "stat-item") {
                        span(classes = "stat-label") { +"Total Analyses" }
                        span(classes = "stat-value") { +"0" }
                    }
                    div(classes = "stat-item") {
                        span(classes = "stat-label") { +"Budgets Analyzed" }
                        span(classes = "stat-value") { +"0" }
                    }
                }
            }
        }
    }

    // Analysis Dialog
    DialogComponent(context) {
        h2(classes = "heading-large") {
            +"Generate New Analysis"
        }
        div(classes = "text-base mt-4") {
            +"Select a budget and analysis type to get personalized insights"
        }
        div {
            attributes["id"] = "response-message"
        }
        AIInsightForm(context, budgets)
    }
}