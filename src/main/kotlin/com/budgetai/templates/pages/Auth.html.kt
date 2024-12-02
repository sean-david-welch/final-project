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

    div(classes = "auth-content") {
        // Initialize Alpine.js state
        attributes["x-data"] = "{activeTab: 'login'}"

        div(classes = "auth-tabs") {
            button(classes = "tab-button") {
                attributes["x-on:click"] = "activeTab = 'login'"
                attributes["x-bind:class"] = "{'active': activeTab === 'login'}"
                +"Login"
            }
            button(classes = "tab-button") {
                attributes["x-on:click"] = "activeTab = 'register'"
                attributes["x-bind:class"] = "{'active': activeTab === 'register'}"
                +"Register"
            }
        }

        div(classes = "forms-container relative") {
            div(classes = "form-panel absolute w-full") {
                attributes["x-show"] = "activeTab === 'login'"
                attributes["x-transition:enter"] = "transform transition ease-out duration-300"
                attributes["x-transition:enter-start"] = "opacity-0 translate-x-4"
                attributes["x-transition:enter-end"] = "opacity-100 translate-x-0"
                attributes["x-transition:leave"] = "transform transition ease-in duration-300"
                attributes["x-transition:leave-start"] = "opacity-100 translate-x-0"
                attributes["x-transition:leave-end"] = "opacity-0 -translate-x-4"
                loginForm()
            }
            div(classes = "form-panel absolute w-full") {
                attributes["x-show"] = "activeTab === 'register'"
                attributes["x-transition:enter"] = "transform transition ease-out duration-300"
                attributes["x-transition:enter-start"] = "opacity-0 translate-x-4"
                attributes["x-transition:enter-end"] = "opacity-100 translate-x-0"
                attributes["x-transition:leave"] = "transform transition ease-in duration-300"
                attributes["x-transition:leave-start"] = "opacity-100 translate-x-0"
                attributes["x-transition:leave-end"] = "opacity-0 -translate-x-4"
                registerForm()
            }
        }
    }
}