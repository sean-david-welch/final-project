package com.budgetai.templates.pages

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

fun createDashboardPage(context: BaseTemplateContext) = DashboardTemplate("Dashboard Overview", context) {
    div(classes = "stats-grid") {
        repeat(3) { index ->
            div(classes = "stat-card") {
                div(classes = "stat-label") {

                }
                div(classes = "stat-value") {

                }
            }
        }
    }
    SpreadsheetComponent()
}