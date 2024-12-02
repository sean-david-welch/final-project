package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
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
    // Logo and Welcome Message
    div(classes = "auth-header") {
        div(classes = "logo-container") {
            // Logo placeholder
            div(classes = "logo-placeholder")
        }
        h1(classes = "welcome-text") { +"Welcome to BudgetAI" }
        p(classes = "subtitle") {
            +"Take control of your finances with AI-powered insights"
        }
    }

    // Auth Tabs
    div(classes = "auth-tabs") {
        button(classes = "tab-button active") { +"Login" }
        button(classes = "tab-button") { +"Register" }
    }

    // Login Form
    form(classes = "auth-form login-form") {
        div(classes = "form-group") {
            label { +"Email" }
            input(type = InputType.email, classes = "input-field") {
                placeholder = "your@email.com"
                required = true
            }
        }
        div(classes = "form-group") {
            label { +"Password" }
            div(classes = "password-input-wrapper") {
                input(type = InputType.password, classes = "input-field") {
                    placeholder = "••••••••"
                    required = true
                }
                button(type = ButtonType.button, classes = "show-password-button") {
                    +"Show"
                }
            }
        }
        div(classes = "form-options") {
            div(classes = "remember-me") {
                input(type = InputType.checkBox, classes = "checkbox")
                label { +"Remember me" }
            }
            a(href = "/forgot-password", classes = "forgot-password") {
                +"Forgot password?"
            }
        }
        button(type = ButtonType.submit, classes = "submit-button") {
            +"Sign In"
        }
    }

    // Register Form (hidden by default)
    form(classes = "auth-form register-form hidden") {
        div(classes = "form-group") {
            label { +"Full Name" }
            input(type = InputType.text, classes = "input-field") {
                placeholder = "John Doe"
                required = true
            }
        }
        div(classes = "form-group") {
            label { +"Email" }
            input(type = InputType.email, classes = "input-field") {
                placeholder = "your@email.com"
                required = true
            }
        }
        div(classes = "form-group") {
            label { +"Password" }
            div(classes = "password-input-wrapper") {
                input(type = InputType.password, classes = "input-field") {
                    placeholder = "••••••••"
                    required = true
                }
                button(type = ButtonType.button, classes = "show-password-button") {
                    +"Show"
                }
            }
        }
        div(classes = "form-group") {
            label { +"Confirm Password" }
            div(classes = "password-input-wrapper") {
                input(type = InputType.password, classes = "input-field") {
                    placeholder = "••••••••"
                    required = true
                }
            }
        }
        div(classes = "terms-agreement") {
            input(type = InputType.checkBox, classes = "checkbox")
            label {
                +"I agree to the "
                a(href = "/terms", classes = "link") { +"Terms of Service" }
                +" and "
                a(href = "/privacy", classes = "link") { +"Privacy Policy" }
            }
        }
        button(type = ButtonType.submit, classes = "submit-button") {
            +"Create Account"
        }
    }

    // Social Login Options
    div(classes = "social-login") {
        p(classes = "divider") { +"Or continue with" }
        div(classes = "social-buttons") {
            button(classes = "social-button") {
                // Google icon placeholder
                div(classes = "social-icon google")
                +"Google"
            }
            button(classes = "social-button") {
                // Apple icon placeholder
                div(classes = "social-icon apple")
                +"Apple"
            }
        }
    }
}