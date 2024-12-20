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
    div(classes = "management-header") {
        div {
            h2(classes = "overview-title") { +"Personal Dashboard" }
            p(classes = "overview-description") {
                +"Create new budgets and view historial accounts"
            }
        }
        a(href = "/dashboard/budget-management", classes = "action-button") {
            +"Budgets Overview"
        }
    }

    SpreadsheetComponent(context)
}