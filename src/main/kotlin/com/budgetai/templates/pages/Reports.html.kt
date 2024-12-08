package com.budgetai.templates.pages

import com.budgetai.models.BudgetDTO
import com.budgetai.models.BudgetItemDTO
import com.budgetai.models.CategoryDTO
import com.budgetai.templates.components.BudgetAnalysisCard
import com.budgetai.templates.components.CategoryBreakdownCard
import com.budgetai.templates.components.SavingsTrackingCard
import com.budgetai.templates.components.SpendingSummaryCard
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
        SpendingSummaryCard()
        BudgetAnalysisCard()
        CategoryBreakdownCard()
        SavingsTrackingCard()
    }
}
