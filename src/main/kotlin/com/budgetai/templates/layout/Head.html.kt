package com.budgetai.templates.layout

import kotlinx.html.*
import io.ktor.server.html.*
import com.budgetai.templates.components.*

class HeadTemplate : Template<HTML> {
    var title: String = "BudgetAI"

    override fun HTML.apply() {
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            title { +title }

            link(rel = "stylesheet", href = "https://cdn.tailwindcss.com")
            script(src = "https://unpkg.com/htmx.org@1.9.10") {}
            script(src = "https://unpkg.com/alpinejs@3.13.3/dist/cdn.min.js") {
                attributes["defer"] = "true"
            }
        }
    }
}