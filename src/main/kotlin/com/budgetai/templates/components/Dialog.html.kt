package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.DialogComponent(context: BaseTemplateContext) {
    dialog {
        attributes["x-data"] = """
            {
                visible: false,
                show() { 
                    this.${'$'}el.showModal();
                    this.${'$'}el.classList.add('dialog-visible');
                },
                close() { 
                    this.${'$'}el.close();
                    this.${'$'}el.classList.remove('dialog-visible');
                    this.${'$'}dispatch('dialog-closed');
                }
            }
        """.trimIndent()
        attributes["x-show"] = "visible"
        attributes["@show-dialog.window"] = "show()"
        attributes["@hide-dialog.window"] = "close()"
        attributes["class"] = "dialog"

        // Content slot
        div(classes = "dialog-content") {
            attributes["id"] = "dialog-content"
        }

        // Close button
        button(classes = "dialog-btn") {
            attributes["@click"] = "close()"
            attributes["formmethod"] = "dialog"
            +"×"
        }
    }
}