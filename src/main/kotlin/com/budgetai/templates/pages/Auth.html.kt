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
    div("auth-content") {
        attributes["x-data"] = "{ activeTab: 'login' }"

        div(classes = "auth-tabs") {
            button(classes = "tab-button") {
                attributes["x-bind:class"] = "{ 'active': activeTab === 'login' }"
                attributes["onclick"] = "activeTab = 'login'"
                +"Login"
            }
            button(classes = "tab-button") {
                attributes["x-bind:class"] = "{ 'active': activeTab === 'register' }"
                attributes["onclick"] = "activeTab = 'register'"
                +"Register"
            }
        }

        div {
            // Login form wrapper
            div {
                attributes["x-show"] = "activeTab === 'login'"
                attributes["x-transition"] = ""
                loginForm()
            }
            // Register form wrapper
            div {
                attributes["x-show"] = "activeTab === 'register'"
                attributes["x-transition"] = ""
                registerForm()
            }
        }
    }
}