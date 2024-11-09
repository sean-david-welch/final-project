package com.budgetai.templates.pages

import com.budgetai.templates.layout.MainLayout
import kotlinx.html.*
import io.ktor.server.html.*

class DashboardPage : Template<HTML> {
    private val layout = MainLayout().apply {
        content {
            div("grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6") {
                // Dashboard widgets
                dashboardCard("Total Balance", "$2,500.00")
                dashboardCard("Monthly Spend", "$750.00")
                dashboardCard("Savings", "$1,750.00")
            }
        }
    }

    private fun FlowContent.dashboardCard(title: String, value: String) {
        div("bg-white rounded-lg shadow p-6") {
            h3("text-gray-700 text-lg font-semibold") { +title }
            p("text-3xl font-bold mt-2") { +value }
        }
    }

    override fun HTML.apply() {
        insert(layout)
    }
}