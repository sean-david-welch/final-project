package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.templates.components.loginForm
import com.budgetai.templates.components.registerForm
import com.budgetai.utils.BaseTemplateContext
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

fun createAuthPage(context: BaseTemplateContext) = AuthTemplate {
    if (context.auth.isAuthenticated) { h1 { +"hello" }}
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

        div(classes = "forms-container") {
            div {
                attributes["x-show"] = "activeTab === 'login'"
                attributes["x-transition"] = ""
                loginForm()
            }
            div {
                attributes["x-show"] = "activeTab === 'register'"
                attributes["x-transition"] = ""
                registerForm()
            }
        }
    }
}