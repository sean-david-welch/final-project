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

    SpreadsheetComponent(context)
}