package com.budgetai.templates.components

import com.budgetai.models.UserDTO
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
            attributes["hx-indicator"] = "#login-response-div"
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
            attributes["class"] = "response-div"
        }
    }
}

fun DIV.registerForm() {
    div {
        form {
            attributes["class"] = "auth-form register-form"
            attributes["hx-post"] = "/auth/register"
            attributes["hx-trigger"] = "submit"
            attributes["hx-target"] = "#register-response-div"
            attributes["hx-indicator"] = "#register-response-div"
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
            attributes["id"] = "register-response-div"
            attributes["class"] = "response-div"
        }
    }
}

fun DIV.updateProfileForm(user: UserDTO) {
    div {
        form {
            attributes["class"] = "settings-form"
            attributes["hx-put"] = "/api/users/${user.id}"
            attributes["hx-trigger"] = "submit"
            attributes["hx-target"] = "#profile-response-div"
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

            formField("New Password", InputType.password, "••••••••") {
                name = "password"
                required = false
                attributes["minlength"] = "8"
            }

            submitButton("Update Profile")
        }

        div {
            attributes["id"] = "profile-response-div"
            attributes["class"] = "response-div"
        }
    }
}

fun FlowContent.logoutButton() {
    form(action = "/auth/logout", method = FormMethod.post) {
        button(type = ButtonType.submit, classes = "nav-item-inactive login") {
            style = "background: none; border: none; padding: 0; cursor: pointer;"
            +"Logout"
        }
    }
}