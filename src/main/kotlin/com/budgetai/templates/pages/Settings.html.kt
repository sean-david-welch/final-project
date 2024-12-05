package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun SettingsTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "settings-layout") {
        div(classes = "settings-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createSettingsPage(context: BaseTemplateContext) = SettingsTemplate("Account Settings", context) {
    div(classes = "settings-grid") {
        // Profile Settings
        div(classes = "settings-section") {
            h2(classes = "section-title") { +"Profile Settings" }
            div(classes = "settings-form") {
                div(classes = "form-group") {
                    label { +"Name" }
                    input(type = InputType.text, classes = "input-field")
                }
                div(classes = "form-group") {
                    label { +"Email" }
                    input(type = InputType.email, classes = "input-field")
                }
                div(classes = "form-group") {
                    label { +"Password" }
                    input(type = InputType.password, classes = "input-field")
                }
                button(classes = "save-button") { +"Update Profile" }
            }
        }

        // Budget Categories
        div(classes = "settings-section") {
            h2(classes = "section-title") { +"Budget Categories" }
            div(classes = "category-list") {
                div(classes = "category-item") {
                    span(classes = "category-name") {}
                    div(classes = "category-actions") {
                        button(classes = "edit-button") { +"Edit" }
                        button(classes = "delete-button") { +"Delete" }
                    }
                }
                button(classes = "add-button") { +"Add Category" }
            }
        }
        // Admin Panel Link - Only shown to admins
        if (context.auth.isAdmin) {
            div(classes = "admin-access-section") {
                a(href = "/admin", classes = "admin-link-button") {
                    +"Access Admin Panel"
                }
            }
        }
    }
}