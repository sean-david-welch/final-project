package com.budgetai.templates.components

import kotlinx.html.*

private fun FORM.formField(
    label: String,
    type: InputType,
    placeholder: String,
    showPasswordToggle: Boolean = false
) {
    div(classes = "form-group") {
        label { +label }
        if (showPasswordToggle) {
            div(classes = "password-input-wrapper") {
                input(type = type, classes = "input-field") {
                    this.placeholder = placeholder
                    required = true
                }
                button(type = ButtonType.button, classes = "show-password-button") {
                    +"Show"
                }
            }
        } else {
            input(type = type, classes = "input-field") {
                this.placeholder = placeholder
                required = true
            }
        }
    }
}

private fun FORM.submitButton(text: String) {
    button(type = ButtonType.submit, classes = "submit-button") {
        +text
    }
}

fun DIV.loginForm() {
    form(classes = "auth-form login-form") {
        formField("Email", InputType.email, "your@email.com")
        formField("Password", InputType.password, "••••••••", showPasswordToggle = true)
        submitButton("Sign In")
    }
}

fun DIV.registerForm() {
    form(classes = "auth-form register-form hidden") {
        formField("Full Name", InputType.text, "John Doe")
        formField("Email", InputType.email, "your@email.com")
        formField("Password", InputType.password, "••••••••", showPasswordToggle = true)
        formField("Confirm Password", InputType.password, "••••••••", showPasswordToggle = true)

        div(classes = "terms-agreement") {
            input(type = InputType.checkBox, classes = "checkbox")
            label {
                +"I agree to the "
                a(href = "/terms", classes = "link") { +"Terms of Service" }
                +" and "
                a(href = "/privacy", classes = "link") { +"Privacy Policy" }
            }
        }
        submitButton("Create Account")
    }
}