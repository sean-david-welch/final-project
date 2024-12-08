package com.budgetai.templates.components

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun FlowContent.DialogComponent(context: BaseTemplateContext) {
    dialog {
        attributes["x-data"] = """
            {
                visible: false,
                show() { 
                    this.$el.showModal();
                    this.$el.classList.add('dialog-visible');
                },
                close() { 
                    this.$el.close();
                    this.$el.classList.remove('dialog-visible');
                    this.$dispatch('dialog-closed');
                }
            }
        """.trimIndent()
        attributes["x-show"] = "visible"
        attributes["@show-dialog.window"] = "show()"
        attributes["@hide-dialog.window"] = "close()"
        attributes["class"] = "dialog"

        // Content slot
        div(classes = "dialog-content") {
            // This is where the content will be injected
            attributes["id"] = "dialog-content"
        }

        // Close button
        button(classes = "dialog-btn") {
            attributes["@click"] = "close()"
            attributes["formmethod"] = "dialog"

            // SVG close icon
            unsafe {
                +"""
                    <svg xmlns="http://www.w3.org/2000/svg" 
                        viewBox="0 0 384 512" 
                        width="14"
                        height="14">
                        <path d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7 86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256 41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3 297.4 406.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256 342.6 150.6z"/>
                    </svg>
                """.trimIndent()
            }
        }
    }
}