package com.budgetai.templates.pages

import com.budgetai.models.BudgetDTO
import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.CategoryDTO
import com.budgetai.templates.components.SpreadsheetComponent
import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun DashboardTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createDashboardPage(
    context: BaseTemplateContext, budgetItems: List<BudgetItemDTO>, budgets: List<BudgetDTO>, categories: List<CategoryDTO>
) = DashboardTemplate("Dashboard Overview", context) {
    div(classes = "overview-header") {
        h2(classes = "overview-title") { +"Your Personal Dashboard" }
        p(classes = "overview-description") {
            +"View usage stats and create new budgets"
        }
    }

    div(classes = "admin-access-section") {
        a(href = "/dashboard/budget-management", classes = "admin-link-button-sm") {
            +"Budgets Overview"
        }
    }

    SpreadsheetComponent(context)
}