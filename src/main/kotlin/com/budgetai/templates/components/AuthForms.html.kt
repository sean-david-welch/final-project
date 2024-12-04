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
    div {
        form {
            attributes["class"] = "auth-form login-form"
            attributes["hx-post"] = "/auth/login"
            attributes["hx-trigger"] = "submit"
            attributes["hx-target"] = "#login-response-div"
            attributes["hx-swap"] = "innerHTML"
            attributes["hx-on::after-request"] = "if(event.detail.successful) this.reset()"

            formField("Email", InputType.email, "your@email.com") {
                name = "email"
                required = true
            }
            formField("Password", InputType.password, "••••••••") {
                name = "password"
                required = true
            }
            submitButton("Sign In")
        }
        div {
            attributes["id"] = "login-response-div"
        }
    }
}

fun DIV.registerForm() {
    div {
        form {
            attributes["class"] = "auth-form register-form"
            attributes["hx-post"] = "/auth/register"
            attributes["hx-trigger"] = "submit"
            attributes["hx-target"] = "#register-repsponse-div"
            attributes["hx-swap"] = "innerHTML"
            attributes["hx-on::after-request"] = "if(event.detail.successful) this.reset()"

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
            submitButton("Create Account")
        }
        div {
            attributes["id"] = "register-repsponse-div"
        }
    }
}

fun FlowContent.logoutButton() {
    a(classes = "nav-item-inactive") {
        attributes["hx-post"] = "/auth/logout"
        attributes["hx-boost"] = "false"
        attributes["hx-indicator"] = "#logout-loading"
        span { +"Logout" }
        span(classes = "loading-indicator htmx-indicator") { id = "logout-loading" + "..." }
    }
}