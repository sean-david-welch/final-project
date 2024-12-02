package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.templates.components.loginForm
import com.budgetai.templates.components.registerForm
import kotlinx.html.*

fun AuthTemplate(contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "auth-layout") {
        div(classes = "auth-container") {
            div(classes = "auth-card") {
                contentFn()
            }
        }
    }
}

fun createAuthPage() = AuthTemplate {
    div(classes = "auth-header") {
        div(classes = "logo-container") {
            div(classes = "logo-placeholder")
        }
        h1(classes = "welcome-text") { +"Welcome to BudgetAI" }
        p(classes = "subtitle") {
            +"Take control of your finances with AI-powered insights"
        }
    }

    // Add x-data to manage the active tab state
    div(classes = "auth-content") {
        // Initialize Alpine.js state
        attributes["x-data"] = "{activeTab: 'login'}"

        div(classes = "auth-tabs") {
            button(classes = "tab-button") {
                // Use @click instead of onclick for Alpine.js
                attributes["@click"] = "activeTab = 'login'"
                // Use template syntax for class binding
                attributes["x-bind:class"] = "{'active': activeTab === 'login'}"
                +"Login"
            }
            button(classes = "tab-button") {
                attributes["@click"] = "activeTab = 'register'"
                attributes["x-bind:class"] = "{'active': activeTab === 'register'}"
                +"Register"
            }
        }

        // Forms container
        div(classes = "forms-container") {
            // Login form wrapper
            div {
                attributes["x-show.transition"] = "activeTab === 'login'"
                loginForm()
            }
            // Register form wrapper
            div {
                attributes["x-show.transition"] = "activeTab === 'register'"
                registerForm()
            }
        }
    }
}