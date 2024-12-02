package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.*

fun SettingsTemplate(title: String, contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "settings-layout") {
        div(classes = "settings-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") { contentFn() }
        }
    }
}

fun createSettingsPage() = SettingsTemplate("Account Settings") {
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
                button(classes = "save-button") { +"Update Profile" }
            }
        }

        // Budget Categories
        div(classes = "settings-section") {
            h2(classes = "section-title") { +"Budget Categories" }
            div(classes = "category-list") {
                repeat(4) { index ->
                    div(classes = "category-item") {
                        span(classes = "category-name") {
                            +when (index) {
                                0 -> "Shopping"
                                1 -> "Entertainment"
                                2 -> "Travel"
                                else -> "Dining"
                            }
                        }
                        div(classes = "category-actions") {
                            button(classes = "edit-button") { +"Edit" }
                            button(classes = "delete-button") { +"Delete" }
                        }
                    }
                }
                button(classes = "add-button") { +"Add Category" }
            }
        }

        // Advanced Settings
        div(classes = "settings-section") {
            h2(classes = "section-title") { +"Advanced Settings" }
            div(classes = "settings-list") {
                div(classes = "setting-item") {
                    div(classes = "setting-content") {
                        h3(classes = "setting-title") { +"Currency" }
                    }
                    div(classes = "setting-control") {
                        select(classes = "select-field") {
                            option { +"USD" }
                            option { +"EUR" }
                            option { +"GBP" }
                        }
                    }
                }
            }
        }
    }
}