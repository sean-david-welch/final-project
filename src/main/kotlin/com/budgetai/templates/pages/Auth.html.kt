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

    div(classes = "auth-tabs") {
        button(classes = "tab-button active") { +"Login" }
        button(classes = "tab-button") { +"Register" }
    }

    loginForm()
    registerForm()
}