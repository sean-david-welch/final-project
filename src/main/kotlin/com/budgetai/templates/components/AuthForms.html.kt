package com.budgetai.templates.components

import kotlinx.html.*

private fun FORM.formField(
    label: String, type: InputType, placeholder: String, showPasswordToggle: Boolean = false, inputConfig: INPUT.() -> Unit = {}
) {
    div(classes = "form-group") {
        label { +label }
        if (showPasswordToggle) {
            div(classes = "password-input-wrapper") {
                input(type = type, classes = "input-field") {
                    this.placeholder = placeholder
                    required = true
                    inputConfig()
                }
                button(type = ButtonType.button, classes = "show-password-button") {
                    +"Show"
                }
            }
        } else {
            input(type = type, classes = "input-field") {
                this.placeholder = placeholder
                required = true
                inputConfig()
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
    form {
        attributes["class"] = "auth-form login-form"
        attributes["hx-post"] = "/auth/login"
        attributes["hx-trigger"] = "submit"
        attributes["hx-target"] = "#response-div"
        attributes["hx-indicator"] = "#loading"

        formField("Email", InputType.email, "your@email.com")
        formField("Password", InputType.password, "••••••••", showPasswordToggle = true)
        submitButton("Sign In")

        div {
            attributes["id"] = "response-div"
        }
        div {
            attributes["id"] = "loading"
            attributes["class"] = "htmx-indicator"
            attributes["style"] = "display: none;"
            +"Loading..."
        }
    }
}

fun DIV.registerForm() {
    form {
        attributes["class"] = "auth-form register-form"
        attributes["hx-post"] = "/auth/register"  // Changed to register endpoint
        attributes["hx-trigger"] = "submit"
        attributes["hx-target"] = "#response-div"
        attributes["hx-indicator"] = "#loading"

        formField("Full Name", InputType.text, "John Doe") {
            name = "fullName"
            required = true
        }
        formField("Email", InputType.email, "your@email.com") {
            name = "email"
            required = true
        }
        formField("Password", InputType.password, "••••••••", showPasswordToggle = true) {
            name = "password"
            required = true
            attributes["minlength"] = "8"  // Optional: add password requirements
        }
        formField("Confirm Password", InputType.password, "••••••••", showPasswordToggle = true) {
            name = "confirmPassword"
            required = true
            attributes["minlength"] = "8"
        }

        div(classes = "terms-agreement") {
            input(type = InputType.checkBox) {
                name = "termsAccepted"
                required = true
                classes = setOf("checkbox")
            }
            label {
                +"I agree to the "
                a(href = "/terms", classes = "link") { +"Terms of Service" }
                +" and "
                a(href = "/privacy", classes = "link") { +"Privacy Policy" }
            }
        }

        submitButton("Create Account")

        // Add response and loading divs
        div {
            attributes["id"] = "response-div"
        }
        div {
            attributes["id"] = "loading"
            attributes["class"] = "htmx-indicator"
            attributes["style"] = "display: none;"
            +"Loading..."
        }
    }
}