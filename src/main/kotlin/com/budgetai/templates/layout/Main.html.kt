package com.budgetai.templates.layout

import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun BaseTemplate(context: BaseTemplateContext, bodyFn: BODY.() -> Unit) = "<!DOCTYPE html>" + createHTML().html {
    lang = "en"
    head {
        meta { charset = "UTF-8" }
        meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
        script { src = "https://unpkg.com/alpinejs@3.14.3/dist/cdn.min.js"; defer = true }
        script { src = "https://unpkg.com/htmx.org@1.9.3/dist/htmx.min.js"; defer = true }
        link { href = "/static/images/favicon.ico"; rel = "icon"; type = "image/x-icon" }
        link { href = "/static/styles/output.css"; rel = "stylesheet" }
        link { href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"; rel = "stylesheet" }

    }
    body(classes = "font-inter min-h-screen bg-gray-50 dark:bg-gray-800") {
        Navbar(context)
        bodyFn()
        Footer(context)
    }
}