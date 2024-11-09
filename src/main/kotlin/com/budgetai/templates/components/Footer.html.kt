package com.budgetai.templates.components

import kotlinx.html.*
import io.ktor.server.html.*

class FooterComponent : Template<FlowContent> {
    override fun FlowContent.apply() {
        footer {
            attributes["class"] = "bg-gray-50 border-t"
            div {
                attributes["class"] = "container mx-auto px-4 py-8"
                div {
                    attributes["class"] = "text-center text-gray-500"
                    +"© ${java.time.Year.now().value} BudgetAI. All rights reserved."
                }
            }
        }
    }
}