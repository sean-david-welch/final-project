package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.h1
import kotlinx.html.main

import kotlinx.html.*

fun HomeTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "home-layout") {
        div(classes = "home-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createHomePage(context: BaseTemplateContext) = HomeTemplate("Welcome to BudgetAI", context) {
    // Hero Section
    div(classes = "hero-section") {
        div(classes = "hero-content") {
            h2(classes = "hero-title") { +"Take Control of Your Finances" }
            p(classes = "hero-description") {
                +"Smart budgeting powered by artificial intelligence to help you achieve your financial goals."
            }
            div(classes = "hero-actions") {
                a(href = "/login", classes = "primary-button") { +"Get Started" }
            }
        }
    }

    // Features Grid
    div(classes = "features-grid") {
        repeat(3) { index ->
            div(classes = "feature-card") {
                div(classes = "feature-icon") {
                    // Icon placeholder
                    div(classes = "icon-placeholder")
                }
                div(classes = "feature-content") {
                    div(classes = "feature-title") {
                        +when (index) {
                            0 -> "Smart Analysis"
                            1 -> "Personalized Insights"
                            else -> "Real-time Tracking"
                        }
                    }
                    div(classes = "feature-description") {
                        +when (index) {
                            0 -> "AI-powered analysis of your spending patterns"
                            1 -> "Custom recommendations based on your goals"
                            else -> "Monitor your finances as they happen"
                        }
                    }
                }
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