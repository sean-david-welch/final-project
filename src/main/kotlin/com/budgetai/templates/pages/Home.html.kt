package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.h1
import kotlinx.html.main

import kotlinx.html.*

fun HomeTemplate(title: String, contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "home-layout") {
        div(classes = "home-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createHomePage() = HomeTemplate("Welcome to BudgetAI") {
    // Hero Section
    div(classes = "hero-section") {
        div(classes = "hero-content") {
            h2(classes = "hero-title") { +"Take Control of Your Finances" }
            p(classes = "hero-description") {
                +"Smart budgeting powered by artificial intelligence to help you achieve your financial goals."
            }
            div(classes = "hero-actions") {
                a(href = "/dashboard", classes = "primary-button") { +"Get Started" }
                a(href = "/learn-more", classes = "secondary-button") { +"Learn More" }
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

    // Testimonials Section
    div(classes = "testimonials-section") {
        h2(classes = "section-title") { +"What Our Users Say" }
        div(classes = "testimonials-grid") {
            repeat(2) { index ->
                div(classes = "testimonial-card") {
                    p(classes = "testimonial-text") {
                        +"BudgetAI has completely transformed how I manage my finances. The insights are incredibly helpful!"
                    }
                    div(classes = "testimonial-author") {
                        +"Sarah ${index + 1}"
                    }
                }
            }
        }
    }
}