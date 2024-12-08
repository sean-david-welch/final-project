package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.DialogComponent(context: BaseTemplateContext, content: FlowContent.() -> Unit) {
    if (context.auth.isAuthenticated) {
        script(src = "/static/scripts/dialog.js") {}

        dialog {
            attributes["x-data"] = "dialogComponent()"
            attributes["x-show"] = "visible"
            attributes["x-on:show-dialog.window"] = "show()"
            attributes["x-on:hide-dialog.window"] = "close()"
            attributes["class"] = "dialog"

            // Content slot
            div(classes = "dialog-content") {
                content()
            }

            // Close button
            button(classes = "dialog-btn") {
                attributes["x-on:click"] = "close()"
                attributes["formmethod"] = "dialog"
                +"×"
            }
        }
    }
}