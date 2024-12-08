package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.DialogComponent(context: BaseTemplateContext, content: FlowContent.() -> Unit) {
    if (context.auth.isAuthenticated) {
        dialog {
            attributes["class"] = "dialog"
            attributes["id"] = "modal-dialog"

            // Content slot
            div(classes = "dialog-content") {
                content()
            }

            // Close button
            button(classes = "dialog-btn") {
                attributes["onclick"] = "this.closest('dialog').close()"
                attributes["formmethod"] = "dialog"
                +"×"
            }
        }
    }
}