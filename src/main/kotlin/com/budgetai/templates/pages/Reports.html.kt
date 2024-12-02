package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.*

fun ReportsTemplate(title: String, contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "reports-layout") {
        div(classes = "reports-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createReportsPage() = ReportsTemplate("Financial Reports & Analytics") {
    // Reports Overview Section
    div(classes = "reports-overview") {
        div(classes = "overview-header") {
            h2(classes = "overview-title") { +"Your Financial Overview" }
            p(classes = "overview-description") {
                +"Comprehensive insights into your spending patterns and financial health."
            }
        }
        div(classes = "time-period-selector") {
            select(classes = "period-dropdown") {
                option { +"Last 30 Days" }
                option { +"Last 3 Months" }
                option { +"Last 6 Months" }
                option { +"Year to Date" }
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
                h3(classes = "report-title") { +"Budget Analysis" }
                div(classes = "report-actions") {
                    button(classes = "action-button") { +"Export Data" }
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

    // Insights Section
    div(classes = "insights-section") {
        h2(classes = "section-title") { +"AI-Powered Insights" }
        div(classes = "insights-grid") {
            repeat(3) { index ->
                div(classes = "insight-card") {
                    div(classes = "insight-icon") {
                        // Icon placeholder
                        div(classes = "icon-placeholder")
                    }
                    div(classes = "insight-content") {
                        h4(classes = "insight-title") {
                            +when (index) {
                                0 -> "Spending Patterns"
                                1 -> "Budget Recommendations"
                                else -> "Savings Opportunities"
                            }
                        }
                        p(classes = "insight-description") {
                            +when (index) {
                                0 -> "Your restaurant spending has increased by 15% this month"
                                1 -> "You could save $200 by adjusting your entertainment budget"
                                else -> "Setting aside 5% more could help reach your goal faster"
                            }
                        }
                    }
                }
            }
        }
    }
}