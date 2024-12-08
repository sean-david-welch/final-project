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
    }

    // Main Content
    div(classes = "content-grid") {
        div {
            attributes["id"] = "response-message"
        }
        AIInsightForm(context, budgets)
    }

}