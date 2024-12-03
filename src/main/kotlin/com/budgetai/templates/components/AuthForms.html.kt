package com.budgetai.templates.components

import kotlinx.html.*

private fun FORM.formField(
    label: String, type: InputType, placeholder: String, inputConfig: INPUT.() -> Unit = {}
) {
    div(classes = "form-group") {
        label { +label }
        input(type = type, classes = "input-field") {
            this.placeholder = placeholder
            required = true
            inputConfig()
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
        formField("Password", InputType.password, "••••••••")
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
        attributes["hx-post"] = "/auth/register"
        attributes["hx-trigger"] = "submit"
        attributes["hx-target"] = "#response-div"
        attributes["hx-indicator"] = "#loading"
        attributes["hx-ext"] = "json-enc"
        // Explicitly set the content type header
        attributes["hx-headers"] = """{"Content-Type": "application/json"}"""

        formField("Full Name", InputType.text, "John Doe") {
            name = "name"
            required = true
        }
        formField("Email", InputType.email, "your@email.com") {
            name = "email"
            required = true
        }
        formField("Password", InputType.password, "••••••••") {
            name = "password"
            required = true
            attributes["minlength"] = "8"
        }

        input(type = InputType.hidden) {
            name = "role"
            value = "USER"
        }

        submitButton("Create Account")

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