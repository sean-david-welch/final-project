package com.budgetai.templates.components

import kotlinx.html.*

fun DIV.loginForm() {
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
        button(type = ButtonType.submit, classes = "submit-button") {
            +"Sign In"
        }
    }
}

fun DIV.registerForm() {
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
}