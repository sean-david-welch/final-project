package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.DialogComponent(context: BaseTemplateContext, content: FlowContent.() -> Unit) {
    if (context.auth.isAuthenticated) {
        script(src = "/static/scripts/dialog.js") {}

        div {
            // Wrap dialog in a div with x-data to ensure proper Alpine.js scope
            attributes["x-data"] = "dialogComponent()"

            dialog {
                // Remove x-data from here since it's now on parent
                attributes["x-show"] = "visible"
                attributes["x-on:show-dialog.window"] = "show"  // Remove parentheses
                attributes["x-on:hide-dialog.window"] = "close" // Remove parentheses
                attributes["class"] = "dialog"
                attributes[":class"] = "{'dialog-visible': visible}"

                // Content slot
                div(classes = "dialog-content") {
                    content()
                }

                // Close button
                button(classes = "dialog-btn") {
                    attributes["x-on:click"] = "close"  // Remove parentheses
                    attributes["formmethod"] = "dialog"
                    +"×"
                }
            }
        }
    }
}