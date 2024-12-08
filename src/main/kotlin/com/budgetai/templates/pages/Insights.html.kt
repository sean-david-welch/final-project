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